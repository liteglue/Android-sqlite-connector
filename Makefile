
all: javabuild

javabuild:
	rm -rf build
	mkdir build
	javac -d build src/io/liteglue/*.java
	cd build && jar cf ../sqlite-connector.jar *

clean:
	rm -rf build sqlite-connector.jar

