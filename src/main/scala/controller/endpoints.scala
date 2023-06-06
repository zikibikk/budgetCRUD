package controller

import domain._
import domain.errors._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object endpoints {
  val listBudgetChanges
      : PublicEndpoint[RequestContext, AppError, List[BudgetChange], Any] =
    endpoint.get
      .in("all_changes")
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[List[BudgetChange]])

  val findChangeById
      : PublicEndpoint[(ChangeId, RequestContext), AppError, Option[
        BudgetChange
      ], Any] =
    endpoint.get
      .in("changeN" / path[ChangeId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])
      .out(jsonBody[Option[BudgetChange]])

  val removeBudgetChange
      : PublicEndpoint[(ChangeId, RequestContext), AppError, Unit, Any] =
    endpoint.delete
      .in("changeN" / path[ChangeId])
      .in(header[RequestContext]("X-Request-Id"))
      .errorOut(jsonBody[AppError])

  val createBudgetChange: PublicEndpoint[
    (RequestContext, CreateBudgetChange),
    AppError,
    BudgetChange,
    Any
  ] =
    endpoint.post
      .in("change")
      .in(header[RequestContext]("X-Request-Id"))
      .in(jsonBody[CreateBudgetChange])
      .errorOut(jsonBody[AppError])
      .out(jsonBody[BudgetChange])

}
