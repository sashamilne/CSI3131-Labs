# Makefile

CC = gcc
SRC_DIR = src
BIN_DIR = bin

PROGRAMS = procmon mon calcloop cploop
SOURCES = $(addprefix $(SRC_DIR)/, $(addsuffix .c, $(PROGRAMS)))
BINARIES = $(addprefix $(BIN_DIR)/, $(PROGRAMS))

# Default target
all: $(BINARIES)

# Rule to compile each source file into binary
$(BIN_DIR)/%: $(SRC_DIR)/%.c | $(BIN_DIR)
	$(CC) -o $@ $<

# Ensure bin directory exists
$(BIN_DIR):
	mkdir -p $(BIN_DIR)

# Clean up binaries
clean:
	rm -rf $(BIN_DIR)

.PHONY: all clean
