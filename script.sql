CREATE DATABASE devlibrary;
	USE devlibrary;

	-- LIVROS
	CREATE TABLE LIVRO (
		id_livro INT AUTO_INCREMENT PRIMARY KEY,
		titulo VARCHAR(150) NOT NULL,
		isbn CHAR(13) NOT NULL UNIQUE,
		autor VARCHAR(100) NOT NULL,
		ano_publicacao YEAR,
		genero VARCHAR(50),
		status ENUM('Disponível', 'Emprestado') DEFAULT 'Disponível'
	);
	-- LIVROS
	CREATE TABLE LIVRO (
		id_livro INT AUTO_INCREMENT PRIMARY KEY,
		titulo VARCHAR(150) NOT NULL,
		isbn CHAR(13) NOT NULL UNIQUE,
		autor VARCHAR(100) NOT NULL,
		ano_publicacao YEAR,
		genero VARCHAR(50),
		status ENUM('Disponível', 'Emprestado') DEFAULT 'Disponível'
	);

	-- LEITORES
	CREATE TABLE LEITOR (
		id_leitor INT AUTO_INCREMENT PRIMARY KEY,
		nome VARCHAR(100) NOT NULL,
		matricula VARCHAR(20) NOT NULL UNIQUE,
		email VARCHAR(100),
		telefone VARCHAR(15)
	);

	-- FUNCIONÁRIOS
	CREATE TABLE FUNCIONARIO (
		id_funcionario INT AUTO_INCREMENT PRIMARY KEY,
		nome VARCHAR(100) NOT NULL,
		matricula VARCHAR(20) NOT NULL UNIQUE,
		email VARCHAR(100),
		telefone VARCHAR(15)
	);

	-- EMPRÉSTIMOS
	CREATE TABLE EMPRESTIMO (
		id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
		fk_livro INT NOT NULL,
		fk_leitor INT NOT NULL,
		fk_funcionario INT NOT NULL,
		data_emprestimo DATE NOT NULL,
		data_prevista_devolucao DATE NOT NULL,
		data_devolucao DATE,
		CONSTRAINT fk_emp_livro FOREIGN KEY (fk_livro) REFERENCES LIVRO(id_livro),
		CONSTRAINT fk_emp_leitor FOREIGN KEY (fk_leitor) REFERENCES LEITOR(id_leitor),
		CONSTRAINT fk_emp_func FOREIGN KEY (fk_funcionario) REFERENCES FUNCIONARIO(id_funcionario)
	);
	-- EMPRÉSTIMOS
	CREATE TABLE EMPRESTIMO (
		id_emprestimo INT AUTO_INCREMENT PRIMARY KEY,
		fk_livro INT NOT NULL,
		fk_leitor INT NOT NULL,
		fk_funcionario INT NOT NULL,
		data_emprestimo DATE NOT NULL,
		data_prevista_devolucao DATE NOT NULL,
		data_devolucao DATE,
		CONSTRAINT fk_emp_livro FOREIGN KEY (fk_livro) REFERENCES LIVRO(id_livro),
		CONSTRAINT fk_emp_leitor FOREIGN KEY (fk_leitor) REFERENCES LEITOR(id_leitor),
		CONSTRAINT fk_emp_func FOREIGN KEY (fk_funcionario) REFERENCES FUNCIONARIO(id_funcionario)
	);

	-- MULTAS
	CREATE TABLE MULTA (
		id_multa INT AUTO_INCREMENT PRIMARY KEY,
		fk_emprestimo INT NOT NULL,
		valor DECIMAL(6,2) NOT NULL,
		pago BOOLEAN,
		data_pagamento DATE,
		CONSTRAINT fk_multa_emp FOREIGN KEY (fk_emprestimo) REFERENCES EMPRESTIMO(id_emprestimo)
	);
