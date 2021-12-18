# WebShopAPI

## Routes
- POST /auth/login
    - Content-Type: application/json
    - JSON{"login": "String", "pwdHash": "String"}
    - Ответы:
        - HttpStatusCode.NotFound
            - Пользователь с таким никнеймом не найден.
        - HttpStatusCode.BadRequest
            - Никнейм или пароль пустые поля.
        - HttpStatusCode.OK
            - Успешная авторизация пользователя
            - JSON{"token": "String"}
- POST /auth/register
    - Content-Type: application/json
    - JSON{"login": "String", "pwdHash": "String"}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Ошибка записи в базу данных. Пользователь с таким никнеймом уже существует.
        - HttpStatusCode.BadRequest
            - Никнейм или пароль пустые поля.
            - Длина логина меньше 5 символов.
        - HttpStatusCode.Created
            - Пользователь успешно создан.
- GET /item
    - Ответы:
        - HttpStatusCode.NoContent
            - В базе данных нет товаров.
        - HttpStatusCode.OK
            - JSON{listOf["name": "String", "amount": int, "price": double]}
- POST /item
    - Content-Type: application/json
    - JSON{"name": "String", "amount": int, "price": double}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Количество товара меньше нуля.
            - Цена товара меньше или равна нуля.
            - Такой товар уже существует
            - Пустое имя у товара
        - HttpStatusCode.OK
            - Товар успешно добавлен
- PUT /item/buy
    - Content-Type: application/x-www-form-urlencoded
    - name=[]&amount=[]
    - Ответы:
        - HttpStatusCode.BadRequest
            - Не предоставлен один из параметров.
            - Количество не является корректным числом (int32).
            - Товара с таким именем не существует.
            - Недостаточно товара.
        - HttpStatusCode.OK
            - Количество товара изменено
- PUT /item/add
    - Content-Type: application/x-www-form-urlencoded
    - name=[]&amount=[]
    - Ответы:
        - HttpStatusCode.BadRequest
            - Не предоставлен один из параметров.
            - Количество не является корректным числом (int32).
            - Товара с таким именем не существует.
        - HttpStatusCode.OK
            - Количество товара изменено
# QuizAppAPI
## Routes
- POST /auth/login
    - Content-Type: application/json
    - JSON{"login": "String", "pwdHash": "String"}
    - Ответы:
        - HttpStatusCode.NotFound
            - Пользователь с таким никнеймом не найден.
        - HttpStatusCode.OK
            - Успешная авторизация пользователя
            - JSON{"token": "String"}
- POST /auth/register
    - Content-Type: application/json
    - JSON{"login": "String", "pwdHash": "String"}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Ошибка записи в базу данных. Пользователь с таким никнеймом уже существует.
        - HttpStatusCode.Created
            - Пользователь успешно создан.
- GET /tests
    - Ответы:
        - HttpStatusCode.OK
            - JSON{listOf["id": int, "name": "string", "desc": "string"]}
- GET /tests/{id}
    - Content-Type: application/json
    - JSON{"id": "int"}
    - Ответы:
        - HttpStatusCode.NotFound
            - Тестирование с данным id не найдено
        - HttpStatusCode.OK
            - JSON{"id": int, "name": "string", "desc": "string"}
- POST /tests/sendAnswers
    - Content-Type: application/json
    - JSON{"answers": listOf(int), "testId": int, "username": "string"}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Количество ответов и вопросов не совпадает
        - HttpStatusCode.OK
            - JSON{"resultSum": int}
- GET /questions/{testId}
    - Content-Type: application/json
    - JSON{"id": "int"}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Отсутствует id
        - HttpStatusCode.OK
            - JSON{listOf("id": "string", "testId": int, "value": int, "questionText": "string", "var1": "string", "var2": "string", "var3": "string", "var4": "string", "answer": "string")}
- GET /users/{login}
    - Content-Type: application/json
    - JSON{"login": "string"}
    - Ответы:
        - HttpStatusCode.BadRequest
            - Отсутствует login
        - HttpStatusCode.NotFound
            - Пользователь с таким никнеймом отсутствует
        - HttpStatusCode.OK
            - JSON{"id": int, "login": "string", "lastTestId": int, "lastResult": int}