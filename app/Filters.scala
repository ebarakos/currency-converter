import javax.inject._
import play.api.http.HttpFilters


/**
 needed for swagger-ui
 */
@Singleton
class Filters() extends HttpFilters {

  override val filters = {
    Seq.empty
  }

}
