package main

import (
	"embed"
	"html/template"
	"os"
)

//go:embed templates/*
var f embed.FS

func GetTpl(name string, local bool) *template.Template {
	tplPath := "templates/"
	if local {

		tpl, _ := template.ParseFiles(tplPath + name + ".tpl")
		tpl.Funcs(template.FuncMap{
			"unescapeHTML": unescapeHTML,
		})
		return tpl
	} else {
		tpl, err := template.ParseFS(f, tplPath+name+".tpl")
		if err != nil {
			panic(err)
		}
		tpl.Funcs(template.FuncMap{
			"unescapeHTML": unescapeHTML,
		})
		return tpl
	}
}

func WriteFile(fileName string) *os.File {
	os.OpenFile(fileName, os.O_CREATE, 0o666)
	file, err := os.OpenFile(fileName, os.O_RDWR, 0o666)
	if err != nil {
		panic(err)
	}
	return file
}

func unescapeHTML(s string) template.HTML {
	return template.HTML(s)
}
