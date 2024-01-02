package main

import (
	"fmt"
	"os"
)

var (
	SRC_FOLDER           = "src"
	MAIN_FOLDER          = fmt.Sprintf("%s/main", SRC_FOLDER)
	JAVA_FOLDER          = fmt.Sprintf("%s/java", MAIN_FOLDER)
	RESOURCE_FOLDER      = fmt.Sprintf("%s/resources", MAIN_FOLDER)
	RESOURCE_CONF_FOLDER = fmt.Sprintf("%s/resources/conf", MAIN_FOLDER)
	SPI_FOLDER           = fmt.Sprintf("%s/META-INF/services", RESOURCE_FOLDER)
	FOLDERS              = []string{SRC_FOLDER, MAIN_FOLDER, JAVA_FOLDER, RESOURCE_FOLDER, RESOURCE_CONF_FOLDER, SPI_FOLDER}
)

func PrepareFolder(config *Config) []string {
	var codeFolder string
	var subCodeFolder string
	if _, err := os.Stat(config.Project); os.IsNotExist(err) {
		os.MkdirAll(config.Project, 0755)
		for _, folder := range FOLDERS {
			os.MkdirAll(fmt.Sprintf("%s/%s", config.Project, folder), 0755)
		}
		codeFolder = fmt.Sprintf("%s/%s/%s", config.Project, JAVA_FOLDER, ConvertToFilePath(fmt.Sprintf("%s.%s", config.Group, config.ArtifactId)))
		os.MkdirAll(codeFolder, 0755)
		if config.App {
			subCodeFolder = fmt.Sprintf("%s/%s", codeFolder, "task")
		} else {
			subCodeFolder = fmt.Sprintf("%s/%s", codeFolder, "router")
		}
		os.MkdirAll(subCodeFolder, 0755)

	}

	return []string{
		fmt.Sprintf("%s/%s", config.Project, JAVA_FOLDER),
		fmt.Sprintf("%s/%s", config.Project, RESOURCE_FOLDER),
		fmt.Sprintf("%s/%s", config.Project, RESOURCE_CONF_FOLDER),
		fmt.Sprintf("%s/%s", config.Project, SPI_FOLDER),
		codeFolder,
		subCodeFolder,
	}

}
