package controllers

import javax.inject.Inject
import play.api.libs.ws._
import play.api.mvc._
import play.api.Logger
import play.api.libs.json._
import play.api.libs.json.Reads._
import models._
import play.api.Configuration

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz.{-\/, \/, \/-}


/**
  *
  * Created by Evangelos.
  */
class ConversionController @Inject() (config: Configuration, ws: WSClient, val controllerComponents: ControllerComponents) extends BaseController {

  /** Converts amount to another currency equivalent
    *
    * @param from currency to convert amount from
    * @param to currency to convert amount to
    * @param amount the amount to be converted
    * @return a JSON with the conversion details
    */
  def convert() = Action(parse.json).async { implicit request =>
    val input = request.body.validate[Input]
    input.fold(
      errors => {
        Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors))))
      },
      inputOk => {
        retrieveRates(inputOk.fromCurrency, inputOk.toCurrency).map {
          case (\/-(from), \/-(to)) => {
            val exchange = to / from
            val convertedAmount = (inputOk.amount * exchange).toString
            Logger.debug("Converted amount: " + convertedAmount)
            Ok(Json.obj("exchange" -> exchange, "amount" -> convertedAmount, "original" -> inputOk.amount))
          }
          case (-\/(fromErrorMsg), -\/(toErrorMsg)) =>
            if (fromErrorMsg == toErrorMsg) {
              InternalServerError(Json.obj("message" -> fromErrorMsg))
            } else InternalServerError(Json.obj("message" -> (fromErrorMsg + ". " + toErrorMsg)))
          case (-\/(fromErrorMsg), \/-(_)) => InternalServerError(Json.obj("message" -> fromErrorMsg))
          case (\/-(_), -\/(toErrorMsg)) => InternalServerError(Json.obj("message" -> toErrorMsg))
        } recover {
          case ex: Exception => InternalServerError(Json.obj("message" -> s"Server error : ${ex.getMessage}"))
        }
      }
    )
  }

  def retrieveRates(from: String, to: String): Future[(\/[String, Double], \/[String, Double])] = {
    val url = config.get[String]("url")
    val timeout = config.get[Duration]("timeout")
    val request = ws.url(url).addHttpHeaders("Accept" -> "application/json")
      .withRequestTimeout(timeout)
    request.get().map {
      result =>
        result.status match {
          case 200 => {
            (parseRate(result.json, from), parseRate(result.json, to))
          }
          case _ =>
            val err = "Source exchange rates not available"
            (-\/(err), -\/(err))
        }
    } recover {
      case ex: Exception => {
        val err = s"Exchange API error : ${ex.getMessage}"
        (-\/(err), -\/(err))
      }
    }
  }

  def parseRate(response: JsValue, symbol: String): \/[String, Double] = {
    if (symbol == "EUR") return \/-(1)
    response \ "rates" \ symbol match {
      case rate => {
        try {
          \/-(rate.as[Double])
        } catch {
          case _: Exception => -\/("Currency " + symbol + " not supported")
        }
      }
    }
  }
}