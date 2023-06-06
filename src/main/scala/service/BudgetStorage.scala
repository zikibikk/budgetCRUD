package service

import cats.syntax.applicativeError._
import cats.syntax.either._
import dao.ChangeSQL
import domain._
import domain.errors._
import doobie._
import doobie.implicits._
import tofu.logging.Logging

trait BudgetStorage {
  def list: IOWithRequestContext[Either[InternalError, List[BudgetChange]]]
  def findById(
      id: ChangeId
  ): IOWithRequestContext[Either[InternalError, Option[BudgetChange]]]
  def removeById(id: ChangeId): IOWithRequestContext[Either[AppError, Unit]]
  def create(
      newChange: CreateBudgetChange
  ): IOWithRequestContext[Either[AppError, BudgetChange]]
}

object BudgetStorage {
  private final class Impl(
      sql: ChangeSQL,
      transactor: Transactor[IOWithRequestContext]
  ) extends BudgetStorage {
    override def list
        : IOWithRequestContext[Either[InternalError, List[BudgetChange]]] =
      sql.listAllChanges
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError(_)))

    override def findById(
        id: ChangeId
    ): IOWithRequestContext[Either[InternalError, Option[BudgetChange]]] = {
      sql
        .findById(id)
        .transact(transactor)
        .attempt
        .map(_.leftMap(InternalError))
    }

    override def removeById(
        id: ChangeId
    ): IOWithRequestContext[Either[AppError, Unit]] =
      sql.removeById(id).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft[Unit]
        case Right(Left(error)) => error.asLeft[Unit]
        case _                  => ().asRight[AppError]
      }

    override def create(
        newChange: CreateBudgetChange
    ): IOWithRequestContext[Either[AppError, BudgetChange]] =
      sql.create(newChange).transact(transactor).attempt.map {
        case Left(th)           => InternalError(th).asLeft[BudgetChange]
        case Right(Left(error)) => error.asLeft[BudgetChange]
        case Right(Right(todo)) => todo.asRight[AppError]
      }

  }

  private final class LoggingImpl(storage: BudgetStorage)(implicit
      logging: Logging[IOWithRequestContext]
  ) extends BudgetStorage {

    private def surroundWithLogs[Error, Res](
        inputLog: String
    )(errorOutputLog: Error => (String, Option[Throwable]))(
        successOutputLog: Res => String
    )(
        io: IOWithRequestContext[Either[Error, Res]]
    ): IOWithRequestContext[Either[Error, Res]] =
      for {
        _ <- logging.info(inputLog)
        res <- io
        _ <- res match {
          case Left(error) => {
            val (msg, cause) = errorOutputLog(error)
            cause.fold(logging.error(msg))(cause => logging.error(msg, cause))
          }
          case Right(result) => logging.info(successOutputLog(result))
        }
      } yield res

    override def list
        : IOWithRequestContext[Either[InternalError, List[BudgetChange]]] =
      surroundWithLogs[InternalError, List[BudgetChange]](
        "Getting all budget changes"
      ) { error =>
        (s"Error while getting all changes: ${error.message}", error.cause)
      } { result =>
        s"All todos: ${result.mkString}"
      }(storage.list)

    override def findById(
        id: ChangeId
    ): IOWithRequestContext[Either[InternalError, Option[BudgetChange]]] =
      surroundWithLogs[InternalError, Option[BudgetChange]](
        s"Getting change by id ${id.value}"
      ) { error =>
        (s"Error while getting change: ${error.message}\n", error.cause)
      } { result =>
        s"Found change: ${result.toString}"
      }(storage.findById(id))

    override def removeById(
        id: ChangeId
    ): IOWithRequestContext[Either[AppError, Unit]] =
      surroundWithLogs[AppError, Unit](s"Removing change by id ${id.value}") {
        error =>
          (s"Error while removing change: ${error.message}", error.cause)
      } { _ =>
        s"Successfully removed change with id ${id.value}"
      }(storage.removeById(id))

    override def create(
        newChange: CreateBudgetChange
    ): IOWithRequestContext[Either[AppError, BudgetChange]] =
      surroundWithLogs[AppError, BudgetChange](
        s"Creating todo with params $newChange"
      ) { error =>
        (s"Error while creating change: ${error.message}", error.cause)
      } { change =>
        s"Created change $change"
      }(storage.create(newChange))
  }

  def make(
      sql: ChangeSQL,
      transactor: Transactor[IOWithRequestContext]
  ): BudgetStorage = {
    implicit val logs =
      Logging.Make
        .contextual[IOWithRequestContext, RequestContext]
        .forService[BudgetStorage]
    val storage = new Impl(sql, transactor)
    new LoggingImpl(storage)
  }
}
