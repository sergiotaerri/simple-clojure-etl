src-up:
	docker-compose up -d banco-clj-db-src
target-up:
	docker-compose up -d banco-clj-db-target
src-b:
	docker-compose build --no-cache banco-clj-db-src
	docker-compose up -d banco-clj-db-src
target-b:
	docker-compose build --no-cache banco-clj-db-src
	docker-compose up -d banco-clj-db-target
down:
	docker-compose down
clean:
# Como o usuario do container != o do host
# caso precise buildar novamente (e dai interagir com as pastas do volume)
# Atualmente é necessário remover os volumes manualmente
	sudo rm -rf ./pgdata-*
