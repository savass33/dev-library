CREATE DATABASE devlibrary;
USE devlibrary;

-- AUTORES
CREATE TABLE AUTOR (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    nacionalidade VARCHAR(50)
);

-- LIVROS
CREATE TABLE LIVRO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    isbn CHAR(13) NOT NULL UNIQUE,
    ano_publicacao YEAR,
    genero VARCHAR(50),
    autor INT NOT NULL,
    status ENUM('Disponível', 'Emprestado') DEFAULT 'Disponível',
    CONSTRAINT fk_livro_autor FOREIGN KEY (autor) REFERENCES AUTOR(id)
);

-- LEITORES
CREATE TABLE LEITOR (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefone VARCHAR(15)
);

-- FUNCIONARIOS
CREATE TABLE FUNCIONARIO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    telefone VARCHAR(15)
);

-- EMPRÉSTIMOS
CREATE TABLE EMPRESTIMO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    livro INT NOT NULL,
    leitor INT NOT NULL,
    funcionario INT NOT NULL,
    data_emprestimo DATE NOT NULL,
    data_prevista_devolucao DATE NOT NULL,
    data_devolucao DATE,
    CONSTRAINT fk_emp_livro FOREIGN KEY (livro) REFERENCES LIVRO(id),
    CONSTRAINT fk_emp_leitor FOREIGN KEY (leitor) REFERENCES LEITOR(id),
    CONSTRAINT fk_emp_func FOREIGN KEY (funcionario) REFERENCES FUNCIONARIO(id)
);

-- MULTAS
CREATE TABLE MULTA (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emprestimo INT NOT NULL,
    valor DECIMAL(6,2) NOT NULL,
    pago BOOLEAN,
    data_pagamento DATE,
    CONSTRAINT fk_multa_emp FOREIGN KEY (emprestimo) REFERENCES EMPRESTIMO(id)
);
