package main

import (
	"fmt"
	"io"
	"os"
)

var (
	SRC_FOLDER           = "src"
	MAIN_FOLDER          = fmt.Sprintf("%s/main", SRC_FOLDER)
	JAVA_FOLDER          = fmt.Sprintf("%s/java", MAIN_FOLDER)
	RESOURCE_FOLDER      = fmt.Sprintf("%s/resources", MAIN_FOLDER)
	RESOURCE_CONF_FOLDER = fmt.Sprintf("%s/resources/conf", MAIN_FOLDER)
	SPI_FOLDER           = fmt.Sprintf("%s/META-INF/services", RESOURCE_FOLDER)
	MVN_WRAPPER_FOLDER   = ".mvn/wrapper"
	MVN_FILES            = []string{"mvnw", "mvnw.cmd"}
	MVN_WRAPPER_FILES    = []string{"maven-wrapper.jar", "maven-wrapper.properties", "MavenWrapperDownloader.java"}
	FOLDERS              = []string{SRC_FOLDER, MVN_WRAPPER_FOLDER, MAIN_FOLDER, JAVA_FOLDER, RESOURCE_FOLDER, RESOURCE_CONF_FOLDER, SPI_FOLDER}
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
		CreateMvnEnv(config)

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

func CreateMvnEnv(config *Config) {
	for _, mvn := range MVN_FILES {
		source, sourceErr := os.Open(fmt.Sprintf("./%s", mvn))
		if sourceErr != nil {
			panic(sourceErr)
		}
		defer source.Close()
		destination, _ := os.Create(fmt.Sprintf("%s/%s", config.Project, mvn))
		defer destination.Close()
		_, err := io.Copy(destination, source)
		if err != nil {
			panic(err)
		}
	}
	for _, mvn := range MVN_WRAPPER_FILES {

		source, sourceErr := os.Open(fmt.Sprintf("%s/%s", MVN_WRAPPER_FOLDER, mvn))
		if sourceErr != nil {
			panic(sourceErr)
		}
		defer source.Close()
		destination, destEr_ := os.Create(fmt.Sprintf("%s/%s/%s", config.Project, MVN_WRAPPER_FOLDER, mvn))
		if destEr_ != nil {
			panic(sourceErr)
		}
		defer destination.Close()
		_, err := io.Copy(destination, source)
		if err != nil {
			panic(err)
		}
	}

}
