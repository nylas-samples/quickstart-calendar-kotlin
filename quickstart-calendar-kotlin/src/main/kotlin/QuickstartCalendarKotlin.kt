import com.nylas.NylasClient
import com.nylas.models.*
import io.github.cdimascio.dotenv.dotenv
import spark.kotlin.Http
import spark.kotlin.ignite
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit


fun main(args: Array<String>) {
    val dotenv = dotenv()

    val nylas = NylasClient(
        apiKey = dotenv["NYLAS_API_KEY"],
        apiUri = dotenv["NYLAS_API_URI"]
    )

    val http: Http = ignite()

    http.get("/nylas/auth") {
        val scope = listOf("https://www.googleapis.com/auth/calendar", "https://www.googleapis.com/auth/calendar.events")
        val config : UrlForAuthenticationConfig = UrlForAuthenticationConfig(
            dotenv["NYLAS_CLIENT_ID"],
            "http://localhost:4567/oauth/exchange",
            AccessType.ONLINE,
            AuthProvider.GOOGLE,
            Prompt.DETECT,
            scope,
            true,
            "sQ6vFQN",
            "atejada@gmail.com")

        val url = nylas.auth().urlForOAuth2(config)
        response.redirect(url)
    }

    http.get("/oauth/exchange") {
        val code : String = request.queryParams("code")
        if(code == "") { response.status(401) }
        val codeRequest : CodeExchangeRequest = CodeExchangeRequest(
            "http://localhost:4567/oauth/exchange",
            code,
            dotenv["NYLAS_CLIENT_ID"],
            null,
            null
        )
        try {
            val codeResponse : CodeExchangeResponse = nylas.auth().exchangeCodeForToken(codeRequest)
            request.session().attribute("grant_id",codeResponse.grantId)
            codeResponse.grantId
        }catch (e : Exception){
            e.toString()
        }
    }

    http.get("/nylas/primary-calendar") {
        try {
            val calendarQueryParams: ListCalendersQueryParams =
                ListCalendersQueryParams.Builder().limit(5).build()
            val calendars: List<Calendar> =
                nylas.calendars().list(
                    request.session().attribute("grant_id"),
                    calendarQueryParams
                ).data
            for (calendar in calendars) {
                if (calendar.isPrimary) {
                    request.session().attribute("primary", calendar.id)
                }
            }
            request.session().attribute("primary")
        }catch (e : Exception){
            e.toString()
        }
    }

    http.get("/nylas/list-events") {
        try {
            val eventQuery: ListEventQueryParams =
                ListEventQueryParams(
                    calendarId = request.session().attribute("primary"),
                    limit = 5
                )
            val myEvents: List<Event> =
                nylas.events().list(
                    request.session().attribute("grant_id"),
                    queryParams = eventQuery
                ).data
            myEvents
        }catch (e : Exception){
            e.toString()
        }
    }

    http.get("/nylas/create-event") {
        try {
            val today = LocalDate.now()
            val instant =
                today.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val nowPlus5 = instant.plus(5, ChronoUnit.MINUTES)
            val startTime = nowPlus5.epochSecond
            val nowPlus10 = nowPlus5.plus(35, ChronoUnit.MINUTES)
            val endTime = nowPlus10.epochSecond
            val eventWhenObj: CreateEventRequest.When =
                CreateEventRequest.When.Timespan(startTime.toInt(), endTime.toInt())

            val eventRequest: CreateEventRequest =
                CreateEventRequest.Builder(eventWhenObj).title("Your event title here").build()

            val eventQueryParams: CreateEventQueryParams =
                CreateEventQueryParams(request.session().attribute("primary"))

            val event: Response<Event> =
                nylas.events().create(request.session().attribute("grant_id"), eventRequest, eventQueryParams)
            event
        }catch (e : Exception){
            e.toString()
        }
    }
}