{
 
   {{if not .App}}"server": {
    "port": {{ .Port}}
  },{{ end}}
  {{if .DB}}"db": {
    "port": 5432,
    "host": "127.0.0.1",
    "user": "postgres",
    "password": "postgres",
    "database": "blog",
    "pool": {
      "maxSize": 20
    }
  },{{ end}}
    {{if .Redis}}"redis": {
    "endpoint": "redis://localhost:6380",
    "role ": "MASTER",
    "maxWaitingHandlers": 2048,
    "netClientOptions": {
      "TcpKeepAlive": true,
      "TcpNoDelay": true
    }
  },{{ end}}
  {{if .ES}}"es": {
    "host": "localhost",
    "port": 9200,
    "schema": "http",
    "data-type": "application/json"
  },{{ end}}
  "project":"{{.Project}}"
}