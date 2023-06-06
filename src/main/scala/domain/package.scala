import cats.data.ReaderT
import cats.effect.IO
import derevo.circe.{decoder, encoder}
import derevo.derive
import doobie.util.Read
import io.estatico.newtype.macros.newtype
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.{Codec, Schema}
import tofu.logging.derivation._

import java.time.Instant

package object domain {
  @derive(loggable, encoder, decoder)
  @newtype
  case class ChangeId(value: Long)
  object ChangeId {
    implicit val doobieRead: Read[ChangeId] = Read[Long].map(ChangeId(_))
    implicit val schema: Schema[ChangeId] =
      Schema.schemaForLong.map(l => Some(ChangeId(l)))(_.value)
    implicit val codec: Codec[String, ChangeId, TextPlain] =
      Codec.long.map(ChangeId(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class Comment(value: String)
  object Comment {
    implicit val doobieRead: Read[Comment] = Read[String].map(Comment(_))
    implicit val schema: Schema[Comment] =
      Schema.schemaForString.map(n => Some(Comment(n)))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ChangeDate(value: Instant)
  object ChangeDate {
    implicit val doobieRead: Read[ChangeDate] =
      Read[Long].map(ts => ChangeDate(Instant.ofEpochMilli(ts)))
    implicit val schema: Schema[ChangeDate] = Schema.schemaForString.map(n =>
      Some(ChangeDate(Instant.parse(n)))
    )(_.value.toString)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class ChangeSum(value: Int)
  object ChangeSum {
    implicit val doobieRead: Read[ChangeSum] = Read[Int].map(ChangeSum(_))
    implicit val schema: Schema[ChangeSum] =
      Schema.schemaForInt.map(l => Some(ChangeSum(l)))(_.value)
    implicit val codec: Codec[String, ChangeSum, TextPlain] =
      Codec.int.map(ChangeSum(_))(_.value)
  }

  @derive(loggable, encoder, decoder)
  @newtype
  case class BalanceSum(value: Int)
  object BalanceSum {
    implicit val doobieRead: Read[BalanceSum] = Read[Int].map(BalanceSum(_))
    implicit val schema: Schema[BalanceSum] =
      Schema.schemaForInt.map(l => Some(BalanceSum(l)))(_.value)
    implicit val codec: Codec[String, BalanceSum, TextPlain] =
      Codec.int.map(BalanceSum(_))(_.value)
  }

  type IOWithRequestContext[A] = ReaderT[IO, RequestContext, A]
}
