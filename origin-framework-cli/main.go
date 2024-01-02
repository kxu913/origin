package main

import (
	"encoding/json"
	"flag"
	"fmt"
	"html/template"
	"os"
)

var (
	project_conf string
	local        bool
)

type Config struct {
	Project       string
	Port          int
	Cluster       bool
	Group         string
	ArtifactId    string
	Version       string
	OriginVersion string
	App           bool
	DB            bool
	ES            bool
	Redis         bool
}

func main() {
	flag.StringVar(&project_conf, "project_conf", "./cli.json", "contains a configuration that used to generate the project")
	flag.BoolVar(&local, "local", false, "generate code for source code or not.")
	flag.Parse()
	if local {
		fmt.Printf("use source code to generate code using %s.\n", project_conf)
	} else {
		fmt.Printf("use cli to generate code using %s.\n", project_conf)
	}
	conent, readError := os.ReadFile("cli.json")
	if readError != nil {
		panic(readError)
	}
	config := &Config{}
	parseError := json.Unmarshal(conent, config)
	if parseError != nil {
		panic(parseError)
	}
	folders := PrepareFolder(config)
	generatePOM(config)
	generateGitignore(config)
	generateLogback(config, folders[1])
	generateConfig(config, folders[2])
	generateSPI(config, folders[3])
	generateMain(config, folders[4])
	generateSubCode(config, folders[5])

}

func generatePOM(config *Config) {
	pom := "pom.xml"
	tpl := GetTpl(pom, local)
	file := WriteFile(fmt.Sprintf("%s/%s", config.Project, pom))
	fmt.Println(file.Name())

	defer file.Close()
	data, _ := json.Marshal(&config)
	m := make(map[string]any)
	json.Unmarshal(data, &m)
	m["lt"] = template.HTML("<")

	err := tpl.Execute(file, m)
	if err != nil {
		panic(err)
	}
}

func generateGitignore(config *Config) {
	gitignore := ".gitignore"
	tpl := GetTpl(gitignore, local)
	file := WriteFile(fmt.Sprintf("%s/%s", config.Project, gitignore))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}
}

func generateLogback(config *Config, path string) {
	logback := "logback.xml"
	tpl := GetTpl(logback, local)
	file := WriteFile(fmt.Sprintf("%s/%s", path, logback))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}
}

func generateConfig(config *Config, path string) {
	appConfig := "config.json"
	tpl := GetTpl(appConfig, local)
	file := WriteFile(fmt.Sprintf("%s/%s", path, appConfig))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}
}

func generateSPI(config *Config, path string) {
	var fileName string
	if config.App {
		fileName = "com.origin.framework.spi.OriginTask"

	} else {
		fileName = "com.origin.framework.spi.OriginRouter"

	}
	tpl := GetTpl(fileName, local)
	file := WriteFile(fmt.Sprintf("%s/%s", path, fileName))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}

}

func generateMain(config *Config, path string) {

	mainJava := "Main.java"
	tpl := GetTpl(mainJava, local)
	file := WriteFile(fmt.Sprintf("%s/%s", path, mainJava))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}

}

func generateSubCode(config *Config, path string) {
	fmt.Println(path)
	var java string
	var tpl *template.Template
	if config.App {
		java = "SampleTask.java"
		tpl = GetTpl("task", local)

	} else {
		java = "SampleRouter.java"
		tpl = GetTpl("router", local)

	}

	file := WriteFile(fmt.Sprintf("%s/%s", path, java))
	fmt.Println(file.Name())

	defer file.Close()
	err := tpl.Execute(file, &config)
	if err != nil {
		panic(err)
	}

}
