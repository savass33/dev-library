CREATE DATABASE devlibrary;
USE devlibrary;

-- LIVROS
CREATE TABLE LIVRO (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    isbn CHAR(13) NOT NULL UNIQUE,
    autor VARCHAR(100) NOT NULL,
    ano_publicacao YEAR,
    genero VARCHAR(50),
    status ENUM('Disponível', 'Emprestado') DEFAULT 'Disponível'
);

-- LEITORES
CREATE TABLE LEITOR (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    telefone VARCHAR(15)
);

-- FUNCIONÁRIOS
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
    fk_livro INT NOT NULL,
    fk_leitor INT NOT NULL,
    fk_funcionario INT NOT NULL,
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
    fk_emprestimo INT NOT NULL,
    valor DECIMAL(6,2) NOT NULL,
    pago BOOLEAN,
    data_pagamento DATE,
    CONSTRAINT fk_multa_emp FOREIGN KEY (emprestimo) REFERENCES EMPRESTIMO(id)
);
