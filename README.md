# 📚 DevLibrary

## 🧩 Descrição

O **Devlibrary** é um projeto simples em **Java com JDBC e MySQL**, criado com o objetivo de praticar conceitos fundamentais de **banco de dados**, **DAO (Data Access Object)** e **boas práticas de conexão e manipulação de dados**.

A aplicação permite o cadastro, listagem, busca e remoção de autores em um banco de dados relacional, utilizando a estrutura do pacote **DAO** e a classe de configuração de conexão **ConnectionDB**.

---

## ⚙️ Funcionalidades

* Inserir novo autor
* Listar todos os autores
* Buscar autor por ID
* Remover autor

---

## 🧱 Estrutura do Projeto

```
Devlibrary/
├── src/
│   ├── app/
│   │   └── Main.java
│   ├── config/
│   │   └── ConnectionDB.java
│   ├── dao/
│   │   └── AutorDAO.java
│   ├── model/
│   │   └── Autor.java
│   └── resources/
│       └── database.sql
└── README.md
```

---

## 🗃️ Banco de Dados

```sql
CREATE DATABASE devlibrary;
USE devlibrary;

CREATE TABLE AUTOR (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    nacionalidade VARCHAR(50)
);
```

---

## 💡 Exemplo de Uso

```java
public class Main {
    public static void main(String[] args) {
        AutorDAO autorDAO = new AutorDAO();
        Autor autor = new Autor("J.R.R. Tolkien", "Britânica");

        autorDAO.inserir(autor);
        autorDAO.listarTodos();
    }
}
```

---

## 🧠 Conceitos Aplicados

* **JDBC (Java Database Connectivity)**
* **DAO Pattern** (padrão de separação de responsabilidades)
* **Try-with-resources** para gerenciamento automático de recursos
* **Boas práticas de SQL (PreparedStatement)**

---

## 🧑‍💻 Tecnologias Utilizadas

* **Java 17+**
* **MySQL**
* **JDBC Driver**

---

## 🚀 Como Executar

1. Clone o repositório:

   ```bash
   git clone https://github.com/seuusuario/devlibrary.git
   ```
2. Configure o banco de dados no MySQL e atualize o `ConnectionDB.java` com suas credenciais.
3. Compile e execute a aplicação:

   ```bash
   javac app/Main.java
   java app.Main
   ```

---

## 📄 Licença

Este projeto é de uso educacional e está sob a licença **MIT**.
