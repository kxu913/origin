package main

import (
	"strings"
)

func ConvertToFilePath(pkg string) string {
	return strings.ReplaceAll(pkg, ".", "/")
}
