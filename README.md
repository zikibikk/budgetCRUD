# CRUD сервер доходов и расходов 
 Может быть использован как бэкенд для сервиса ведения бюджета
____
## Запуск
* Первым делом создать базу данных и подключить её к проекту
* Перейти в файл docker.yaml
* Запустить выполнение из файла или из терминала командой docker-compose up 
* В терминале исполнить`docker exec -it <имя_базы_данных> psql -U postgres -d budget`
  где *имя_базы_данных* - имя, назначенное докером. Его можно посмотреть через 
десктопное приложение или выполнив в консоли команду `docker ps`
* Перейти в init.sql и запустить исполнение. Создадутся база данных и две таблицы
* После этого сервер принимает запросы
----
##Сервер принимает запросы 

GET по пути `all_changes` и возвращает все изменения из таблицы

GET по пути `changeN/{id}` и возвращает изменение по id

POST по пути `change`, с заданной *CreateBudgetChange* в заголовке

DELETE по пути `changeN/{id}` и возвращает удалённую запись



