# Execução integrada

## 1. Backend

```powershell
cd backend-consulta-facil-main\agendamento-consultas
.\mvnw.cmd spring-boot:run
ou
mvn spring-boot:run
```

Por padrão, o backend usa um banco H2 persistido em `data/` e cria dados demonstrativos. Para usar MySQL:

```powershell
$env:SPRING_PROFILES_ACTIVE = "mysql"
$env:DB_URL = "jdbc:mysql://localhost:3307/consulta_facil?serverTimezone=America/Sao_Paulo"
$env:DB_USER = "root"
$env:DB_PASSWORD = "sua-senha"
.\mvnw.cmd spring-boot:run
```

## 2. Frontend

Em outro terminal:

```powershell
cd consulta-facil
npm.cmd install
npm.cmd run dev
```

O Vite encaminha `/api` para `http://localhost:8080`. Em produção, configure `VITE_API_URL` com a URL pública da API.
No backend, configure `CORS_ALLOWED_ORIGINS` com as origens permitidas, separadas por vírgula.

## Contas locais

Todas usam a senha `12345678`:

- Paciente: `maria@consultafacil.com`
- Profissional: `ana@consultafacil.com`
- Administrador: `admin@consultafacil.com`

## Verificações

```powershell
cd consulta-facil
npm.cmd run build
npm.cmd run lint
npm.cmd test

cd ..\backend-consulta-facil-main\agendamento-consultas
.\mvnw.cmd test
```
