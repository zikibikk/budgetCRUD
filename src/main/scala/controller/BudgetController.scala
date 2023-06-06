package controller

import cats.effect.IO
import cats.syntax.either._
import controller.endpoints._
import domain.errors._
import service.BudgetStorage
import sttp.tapir.server.ServerEndpoint

trait BudgetController {
  def listAllChanges: ServerEndpoint[Any, IO]
  def findChangeById: ServerEndpoint[Any, IO]
  def removeChangeById: ServerEndpoint[Any, IO]
  def createChange: ServerEndpoint[Any, IO]
  def all: List[ServerEndpoint[Any, IO]]
}

object BudgetController {
  final private class Impl(storage: BudgetStorage) extends BudgetController {

    override val listAllChanges: ServerEndpoint[Any, IO] =
      listBudgetChanges.serverLogic { ctx =>
        storage.list.map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val findChangeById: ServerEndpoint[Any, IO] =
      endpoints.findChangeById.serverLogic { case (id, ctx) =>
        storage.findById(id).map(_.leftMap[AppError](identity)).run(ctx)
      }

    override val removeChangeById: ServerEndpoint[Any, IO] =
      endpoints.removeBudgetChange.serverLogic { case (id, ctx) =>
        storage.removeById(id).run(ctx)
      }

    override val createChange: ServerEndpoint[Any, IO] =
      endpoints.createBudgetChange.serverLogic { case (ctx, todo) =>
        storage.create(todo).run(ctx)
      }

    override val all: List[ServerEndpoint[Any, IO]] = List(
      listAllChanges,
      findChangeById,
      removeChangeById,
      createChange
    )
  }

  def make(storage: BudgetStorage): BudgetController = new Impl(storage)
}
