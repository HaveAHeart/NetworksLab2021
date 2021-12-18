import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.auth.*
import io.ktor.auth.jwt.*

fun Application.registerRoutes() {
    routing {
        routedAPI()
        routedAuth()
    }
}

fun Route.routedAuth() {
    val auth = Auth() //to establish db connection once and use only statements
    route("/auth") {
        post("/login") {
            val authData = call.receive<AuthData>()
            if (auth.login(authData.login, authData.pwdHash)) {
                val jwtToken = JWT.create()
                    .withClaim("login", authData.login)
                    .withClaim("pwdHash", authData.pwdHash)
                    .withSubject("Authentication")
                    .sign(Algorithm.HMAC256("Here is the secret"))
                call.respond(status = HttpStatusCode.OK, AuthSuccess(jwtToken))
            }
            else {
                call.respond(status = HttpStatusCode.NotFound, "There is no such username with these login/password.")
            }
        }
        post("/register") {
            val authData = call.receive<AuthData>()
            if (auth.register(authData.login, authData.pwdHash)) {
                call.respond(status = HttpStatusCode.Created,"Registration successful. Now you can log in using your credentials at auth/login.")
            }
            else {
                call.respond(status = HttpStatusCode.BadRequest, "Something went wrong. Maybe, this username is already taken?")
            }

        }
    }
}

fun Route.routedAPI() {
    val conn = DB.connect()!!

    authenticate("auth-jws") {
        route("/tests") {
            get {
                val resSet = conn.createStatement()
                    .executeQuery("select * from testapp.tests;")
                val tests = mutableListOf<Test>()
                while (resSet.next()) {
                    val id = resSet.getInt(1)
                    val name = resSet.getString(2)
                    val desc = resSet.getString(3)
                    tests.add(Test(id, name, desc))
                }
                call.respond(status = HttpStatusCode.OK, TestsList(tests))
            }
            get("{id}") {
                val reqIdStr = call.parameters["id"] ?: return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )
                val reqId = reqIdStr.toInt()
                val resSet = conn.createStatement()
                    .executeQuery("select * from testapp.tests where id='$reqId';")

                val tests = mutableListOf<Test>()
                while (resSet.next()) {
                    val id = resSet.getInt(1)
                    val name = resSet.getString(2)
                    val desc = resSet.getString(3)
                    tests.add(Test(id, name, desc))
                }
                val test = tests.find { it.id == reqId } ?: return@get call.respondText(
                    "No test with id: $reqId",
                    status = HttpStatusCode.NotFound
                )
                call.respond(test)
            }
            post {
                val test = call.receive<Test>()
                conn.createStatement()
                    .execute("insert into testapp.tests(name, desc) values('${test.name}', '${test.desc}');")
                call.respondText("Test added correctly", status = HttpStatusCode.Created)
            }
            post("/sendAnswers"){
                //getting the user answers
                val answers = call.receive<Answers>()

                //getting the correct answers and their values
                val correctAnswers = mutableListOf<Int>()
                val values = mutableListOf<Int>()
                val questionsSet = conn.createStatement()
                    .executeQuery("select * from testapp.questions where testId='${answers.testId}' order by id;")
                while (questionsSet.next()) {
                    correctAnswers.add(questionsSet.getInt(9))
                    values.add(questionsSet.getInt(3))
                }

                //check if incoming answers are ok
                if (correctAnswers.size != answers.answers.size) {
                    call.respondText("Incorrect amount of answers", status = HttpStatusCode.BadRequest)
                }
                else {
                    //comparing and calculating the sum of values
                    var resultSum = 0
                    for (i in 0 until answers.answers.size) {
                        if (correctAnswers[i] == answers.answers[i]) {
                            resultSum += values[i]
                        }
                    }

                    //updating the user stats
                    val login = answers.username
                    conn.createStatement()
                        .execute("update testapp.users set lastTestId='${answers.testId}', lastResult='$resultSum' where login='$login';")
                    call.respond(status = HttpStatusCode.OK, AnswersResult(resultSum))
                }
            }
        }

        route("/questions") {
            get("{testId}") {
                val testIdInStr = call.parameters["testId"] ?: return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )
                val testIdIn = testIdInStr.toInt()
                val resSet = conn.createStatement()
                    .executeQuery("select * from testapp.questions where testId='$testIdIn' order by id;")
                val questions = mutableListOf<Question>()
                while (resSet.next()) {
                    val id = resSet.getInt(1)
                    val testId = resSet.getInt(2)
                    val value = resSet.getInt(3)
                    val questionText = resSet.getString(4)
                    val var1 = resSet.getString(5)
                    val var2 = resSet.getString(6)
                    val var3 = resSet.getString(7)
                    val var4 = resSet.getString(8)
                    val answer = resSet.getInt(9)
                    val question = Question(id, testId, value, questionText, var1, var2, var3, var4, answer)
                    questions.add(question)
                }
                val questionsList = QuestionsList(questions)
                call.respond(status = HttpStatusCode.OK, questionsList)
            }
            post {
                val q = call.receive<Question>()
                val res = conn.createStatement()
                    .execute(
                        "insert into testapp.tests(testId, value, var1, var2, var3, var4, answer) " +
                                "values('${q.testId}', '${q.value}', '${q.var1}', '${q.var2}', '${q.var3}', '${q.var4}', '${q.answer}')"
                    )
                if (res) {
                    call.respondText("Question added correctly", status = HttpStatusCode.Created)
                }
                else {
                    call.respondText("Error while adding the question", status = HttpStatusCode.BadRequest)
                }
            }
        }

        route("/users") {
            get("{login}") {
                val reqLogin = call.parameters["login"] ?: return@get call.respondText(
                    "Missing or malformed login",
                    status = HttpStatusCode.BadRequest
                )
                val resSet = conn.createStatement()
                    .executeQuery("select * from testapp.users where login='$reqLogin';")

                val users = mutableListOf<User>()
                while (resSet.next()) {
                    val id = resSet.getInt(1)
                    val login = resSet.getString(2)
                    val lastTestId = resSet.getInt(4)
                    val lastResult = resSet.getInt(5)
                    users.add(User(id, login, lastTestId, lastResult))
                }
                val user = users.find { it.login == reqLogin } ?: return@get call.respondText(
                    "No user with login: $reqLogin",
                    status = HttpStatusCode.NotFound
                )
                call.respond(status = HttpStatusCode.OK, user)

            }
        }
    }
}