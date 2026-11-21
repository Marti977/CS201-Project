JAVAC = javac
JAVA = java
# Set to your local JavaFX SDK lib folder if not bundled in your JDK
JAVAFX_LIB ?=

ifdef JAVAFX_LIB
	FX_FLAGS = --module-path $(JAVAFX_LIB) --add-modules javafx.controls,javafx.fxml
else
	FX_FLAGS =
endif

SRCS = $(shell find src -name "*.java")

all:
	mkdir -p bin
	$(JAVAC) $(FX_FLAGS) -d bin $(SRCS)

run: all
	$(JAVA) $(FX_FLAGS) -cp bin ui.Main

clean:
	rm -rf bin