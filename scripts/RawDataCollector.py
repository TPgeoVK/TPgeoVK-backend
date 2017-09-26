## -*- coding: utf-8 -*-

# Скрипт для сбора сырых данных о чекинах vk

import vk_api
import MySQLdb
import _mysql_exceptions
import re


login, password = "login", "password"
vk_sesion = vk_api.VkApi(login, password)

try:
    vk_sesion.auth()
except vk_api.AuthError as err:
    print err


tools = vk_api.VkTools(vk_sesion)
api = vk_sesion.get_api()

# Открываем соединение с базой данных
db = MySQLdb.connect(host="localhost", user="root", passwd="toor", db="VK", charset='utf8', use_unicode=True)
cursor = db.cursor()

pattern = re.compile("["
        u"\U0001F600-\U0001F64F"  # emoticons
        u"\U0001F300-\U0001F5FF"  # symbols & pictographs
        u"\U0001F680-\U0001F6FF"  # transport & map symbols
        u"\U0001F1E0-\U0001F1FF"  # flags (iOS)
                           "]+", flags=re.UNICODE)


'''
# Загружаем сырые данные о местах
# В качестве примера берем места на расстоянии 18 км от  центра Москвы (Красной площади)
# В результате загрузится 2000 объектов
loaded = []
loaded= tools.get_all("places.search", 1000, {"latitude": 55.7539303,
                                                             "longitude": 37.620795,
                                                             "radius": 3,
                                                             "count": 1000})

print "Loaded " + str(loaded['count']) + " places."
places = loaded['items']
totalPlaces = len(places)
succeed = 0
errors = 0
print "Updating database..."

for i in range(0, totalPlaces):
    place = places[i]
    placeTitle = pattern.sub(r'', place['title'])
    sql = "INSERT INTO RawPlaces(placeId, checkins, title) VALUES (" + str(place['id']) + ", " + \
        str(place['checkins']) + ", '" + placeTitle + "') " + \
        "ON DUPLICATE KEY UPDATE checkins=" + str(place['checkins']) + ";"
    try:
        cursor.execute(sql)
        succeed += 1
    except (_mysql_exceptions.ProgrammingError, _mysql_exceptions.OperationalError):
        # Некоторые места (их мало) имеют в заговках нестандартные символы, из-за этого
        # возникают ошибки, свазанные с кодировкой. Исправить их так и не удалось.
        errors += 1
        print "Wrong query at " + str(i) + ": " + sql
        continue


    loadedCheckins = tools.get_all("places.getCheckins", 100, {"place": place["id"],
                                                               "friends_only": 0})
    checkins = loadedCheckins['items']
    for j in range(0, len(checkins)):
        checkin = checkins[j]
        sql = "INSERT INTO RawCheckins(userId, postId, placeId) VALUES (" + str(checkin['user_id']) + ", " + \
           str(checkin['post_id']) + ", " + str(place['id']) + ");"
        try:
            cursor.execute(sql)
            succeed += 1
        except (_mysql_exceptions.ProgrammingError, _mysql_exceptions.OperationalError):
            print "Wrong query at " + str(i) + "," + str(j) + ": " + sql
            errors += 1
print "Commit..."
db.commit()

print "Database updated."
print "Succeed updates: " + str(succeed) + ", errors: " + str(errors)
'''

sql = "SELECT count(id) FROM RawCheckins;"
cursor.execute(sql)
count = cursor.fetchall()[0][0]

succeed = 0
errors = 0

for i in range(1847, count/10 + 1):
    db.commit()
    sql = "SELECT userId, postId FROM RawCheckins LIMIT 10 OFFSET " + str(i*10) + ";"
    cursor.execute(sql)
    result = cursor.fetchall()
    posts = ""
    for rec in result:
        posts = posts + str(rec[0]) + "_" + str(rec[1]) + ","
    posts = posts[:-1]
    response = api.wall.getById(posts=posts)
    for j in range(0, len(response)):
        item = response[j]
        text = item['text']
        if len(text) != 0:
            text = text.replace('"', '')
            text = text.replace("'", '')
            text = text.replace("\n", '')
            text = text.replace("\r", '')
            text = pattern.sub(r'', text)
        sql = "UPDATE awCheckins SET postText='" + text + "' WHERE userId=" + str(item['owner_id']) + \
              " AND postId=" + str(item['id']) + ";"
        try:
            cursor.execute(sql)
            succeed += 1
        except (_mysql_exceptions.ProgrammingError, _mysql_exceptions.OperationalError):
            print "Wrong query at " + str(i) + ", " + str(j) + ": " + sql
            errors += 1
db.commit()

print "Database updated."
print "Succeed updates: " + str(succeed) + ", errors: " + str(errors)

db.close()
print "Database connection closed."
