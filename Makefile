login:
	docker login

build-datomic-transactor:
	docker buildx build --platform linux/amd64 -t alexandrvirtual/datomic-transactor-test:1.0.7364 --push .