package models

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._


/**
  * Created by Evangelos.
  */

/**
  * The Input model, to be used in POST body
  *
  * @param fromCurrency the currency to convert from
  * @param toCurrency the currency to convert to
  * @param amount the amount to be converted

  */

case class Input(
                  fromCurrency: String,
                  toCurrency: String,
                  amount: Double
                )

object Input {

  implicit val requestReads: Reads[Input] = (
    (JsPath \ "fromCurrency").read[String](minLength[String](3) keepAnd maxLength[String](3)) and
      (JsPath \ "toCurrency").read[String](minLength[String](3) keepAnd maxLength[String](3)) and
      (JsPath \ "amount").read[Double]
    )(Input.apply _)

}
