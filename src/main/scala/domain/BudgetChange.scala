package domain

import derevo.circe.{decoder, encoder}
import derevo.derive
import sttp.tapir.Schema
import tofu.logging.derivation._

@derive(loggable, encoder, decoder)
final case class CreateBudgetChange(
    changeSum: ChangeSum,
    changeDate: ChangeDate,
    comment: Comment
)

@derive(loggable, encoder, decoder)
final case class BudgetChange(
    id: ChangeId,
    changeSum: ChangeSum,
    changeDate: ChangeDate,
    comment: Comment
)
object BudgetChange {
  implicit val schema: Schema[BudgetChange] = Schema.derived
}

@derive(loggable, encoder, decoder)
final case class CreateBalance(sum: BalanceSum, changeDate: ChangeDate)

@derive(loggable, encoder, decoder)
final case class Balance(id: Long, sum: BalanceSum, changeDate: ChangeDate)
object Balance {
  implicit val schema: Schema[Balance] = Schema.derived
}
