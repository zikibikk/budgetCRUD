package dao

import cats.syntax.applicative._
import cats.syntax.either._
import doobie._
import doobie.implicits._
import domain._
import domain.errors._

trait ChangeSQL {
  def listAllChanges: ConnectionIO[List[BudgetChange]]
  def findById(id: ChangeId): ConnectionIO[Option[BudgetChange]]
  def removeById(id: ChangeId): ConnectionIO[Either[ChangeNotFound, Unit]]
  def create(
      budgetChange: CreateBudgetChange
  ): ConnectionIO[Either[ChangeAlreadyExists, BudgetChange]]
  def getBalance: ConnectionIO[Option[Balance]]
}

object ChangeSQL {

  object sqls {
    val listAllSql: Query0[BudgetChange] =
      sql"select * from BUDGET".query[BudgetChange]

    def findByIdSql(id: ChangeId): Query0[BudgetChange] =
      sql"select * from BUDGET where id=${id.value}".query[BudgetChange]

    def removeByIdSql(id: ChangeId): Update0 =
      sql"delete from BUDGET where id=${id.value}".update

    def insertSql(newChange: CreateBudgetChange): Update0 =
      sql"insert into BUDGET (change_sum, change_date, comment) values (${newChange.changeSum.value}, ${newChange.changeDate.value
          .toEpochMilli()}, ${newChange.comment.value})".update

    def findBySumAndDate(sum: ChangeSum, date: ChangeDate) =
      sql"select * from BUDGET where change_sum=${sum.value} and changeDate=${date.value.toEpochMilli()}"
        .query[BudgetChange]

    val getBalanceSQL: Query0[Balance] =
      sql"select * from BALANCE ORDER BY balance_date LIMIT 1"
        .query[Balance]

    def changeBalance(newBalance: CreateBalance): Update0 =
      sql"insert into BALANCE (balance_sum, balance_date) values (${newBalance.sum.value}, ${newBalance.changeDate.value
          .toEpochMilli()})".update
  }

  private final class Impl extends ChangeSQL {
    import sqls._

    override def listAllChanges: doobie.ConnectionIO[List[BudgetChange]] =
      listAllSql.to[List]

    override def findById(
        id: ChangeId
    ): doobie.ConnectionIO[Option[BudgetChange]] = findByIdSql(id).option

    override def getBalance: doobie.ConnectionIO[Option[Balance]] =
      getBalanceSQL.option

    override def removeById(
        id: ChangeId
    ): doobie.ConnectionIO[Either[ChangeNotFound, Unit]] =
      removeByIdSql(id).run.map {
        case 0 => ChangeNotFound(id).asLeft[Unit]
        case _ => ().asRight[ChangeNotFound]
      }

    override def create(
        budgetChange: CreateBudgetChange
    ): doobie.ConnectionIO[Either[ChangeAlreadyExists, BudgetChange]] = {
      getBalanceSQL.option.flatMap {
        case Some(value)
            if (value.sum.value + budgetChange.changeSum.value >= 0) =>
          findBySumAndDate(
            budgetChange.changeSum,
            budgetChange.changeDate
          ).option
            .flatMap {
              case None =>
                changeBalance(
                  CreateBalance(
                    BalanceSum(value.sum.value + budgetChange.changeSum.value),
                    budgetChange.changeDate
                  )
                )
                insertSql(budgetChange)
                  .withUniqueGeneratedKeys[ChangeId]("id")
                  .map(id =>
                    BudgetChange(
                      id,
                      budgetChange.changeSum,
                      budgetChange.changeDate,
                      budgetChange.comment
                    ).asRight[ChangeAlreadyExists]
                  )

              case Some(_) =>
                ChangeAlreadyExists().asLeft[BudgetChange].pure[ConnectionIO]
            }
      }
    }
  }

  def make: ChangeSQL = new Impl
}
