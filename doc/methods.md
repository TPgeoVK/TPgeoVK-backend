# Методы API
---

Все методы используют два HTTP-кода ответа: 200 OK в случае успешного выполнения, и 400 Bad Request в случае ошибки.
Базовый URL: *http://tp2017.park.bmstu.cloud/tpgeovk*

## /location

### POST /loaction/detectPlace

Возвращает наиболее вероятное место, в котором находится пользователь в зависимости от координат и текста поста. Принимает на вход объект запроса определения места. Возвращает:
 - Объект места с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

## /recommend

### GET /recommend/event/byFriends
Рекомендует мероприятия в зависимости от координат и друзей пользователя. На вход принимает параметры:
 - **token** - токен пользователя
 - **latitude** - широта
 - **longitude** - долгота

Метод возвращает:
 - Список объектов групп с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

### GET /recommend/event/bySimilarity (не реализовано)
Рекомендует мероприятия в зависимости от координат и предпочтений пользователя. На вход принимает параметры:
 - **token** - токен пользователя
 - **latitude** - широта
 - **longitude** - долгота

Метод возвращает:
 - Список объектов групп с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

### GET /recommend/event/byCheckins (не реализовано)
Рекомендует мероприятия в зависимости от координат и чекинов пользователя. На вход принимает параметры:
 - **token** - токен пользователя
 - **latitude** - широта
 - **longitude** - долгота

Метод возвращает:
 - Список объектов групп с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

### GET /recommend/groups/byCheckins (не реализовано)

### GET /recommend/friends/byCheckins (не реализовано)

## /vkapi

### GET /vkapi/user
Получает информацию о текущем пользователе. На вход принимает параметры:
 - **token** - токен пользователя

Метод возвращает:
 - Объект пользователя с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

### GET /vkapi/checkins/all
Получает все чекины пользователя. На вход принимает параметры:
 - **token** - токен пользователя

Метод возвращает:
 - Список объектов чекинов с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки

### GET /vkapi/checkins/latest
Получает все чекины за последние 15 минут в окрестности заданной точки. На вход принимает параметры:
 - **token** - токен пользователя
 - **latitude** - широта
 - **longitude** - долгота

Метод возвращает:
 - Список объектов чекинов с HTTP-кодом 200 OK  в случае успеха
 - Объект ошибки с HTTP-кодом 401 Bad Request в случае ошибки