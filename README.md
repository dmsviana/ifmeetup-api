# 🎉 IFMeetup - Sistema de Gerenciamento de Eventos Acadêmicos

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Índice

- [🎯 Sobre o Projeto](#-sobre-o-projeto)
- [🏗️ Arquitetura](#️-arquitetura)
- [🚀 Funcionalidades](#-funcionalidades)
- [🛠️ Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [📋 Pré-requisitos](#-pré-requisitos)
- [🔧 Instalação e Configuração](#-instalação-e-configuração)
- [🏃‍♂️ Como Executar](#️-como-executar)
- [📖 Documentação da API](#-documentação-da-api)
- [🧪 Testes](#-testes)
- [🚀 Deploy](#-deploy)
- [🤝 Contribuindo](#-contribuindo)
- [📄 Licença](#-licença)
- [👥 Equipe](#-equipe)

---

## 🎯 Sobre o Projeto

O **IFMeetup** é um sistema completo de gerenciamento de eventos acadêmicos desenvolvido especialmente para o Instituto Federal da Paraíba (IFPB). O sistema permite o cadastro, organização e controle de eventos como palestras, workshops, minicursos, seminários e reuniões acadêmicas.

### 🎯 Objetivos

- **Centralizar** o gerenciamento de eventos acadêmicos do IFPB
- **Facilitar** a inscrição e participação em eventos
- **Automatizar** o processo de aprovação e controle de eventos
- **Melhorar** a comunicação entre organizadores e participantes
- **Gerar** relatórios e certificados de participação
- **Otimizar** o uso dos espaços físicos da instituição

### 🌟 Diferenciais

- ✅ **Interface intuitiva** e responsiva
- ✅ **Sistema de notificações** por email
- ✅ **Controle de acesso** baseado em perfis
- ✅ **Gestão de salas** e recursos
- ✅ **API RESTful** completa
- ✅ **Documentação automática** com Swagger
- ✅ **Segurança** com JWT
- ✅ **Auditoria** completa de ações

---

## 🏗️ Arquitetura

O sistema segue os padrões de **Clean Architecture** e **Domain-Driven Design (DDD)**, organizando o código em camadas bem definidas:

```
src/main/java/br/edu/ifpb/ifmeetup/
├── 🎮 controller/          # Controladores REST
│   ├── AuthController      # Autenticação e autorização
│   └── contract/          # Contratos de API (Swagger)
├── 🗂️ dto/                # Data Transfer Objects
│   ├── auth/              # DTOs de autenticação
│   └── user/              # DTOs de usuário
├── 🏢 domain/             # Domínio da aplicação
│   ├── entity/            # Entidades JPA
│   ├── repository/        # Interfaces de repositório
│   ├── enums/             # Enumerações
│   └── projection/        # Projeções de consulta
├── 🔧 service/            # Serviços de negócio
├── ⚙️ config/             # Configurações
├── 🔒 security/           # Segurança e JWT
└── ❌ exception/          # Tratamento de exceções
```

### 🏛️ Padrões Arquiteturais

- **MVC (Model-View-Controller)**: Separação clara de responsabilidades
- **Repository Pattern**: Abstração da camada de dados
- **DTO Pattern**: Transferência segura de dados
- **Service Layer**: Encapsulamento da lógica de negócio
- **RBAC (Role-Based Access Control)**: Controle de acesso baseado em funções

---

## 🚀 Funcionalidades

### 👤 Gestão de Usuários

- ✅ **Cadastro e autenticação** de usuários
- ✅ **Integração SUAP** - Login com credenciais institucionais do IFPB
- ✅ **Mapeamento automático de perfis** baseado na função no SUAP
- ✅ **Verificação de email** obrigatória
- ✅ **Recuperação de senha** via email
- ✅ **Perfis diferenciados**: Admin, Coordenador, Professor, Estudante
- ✅ **Controle de acesso** baseado em funções
- ✅ **Histórico de login** e auditoria

### 📅 Gestão de Eventos

- ✅ **Criação de eventos** com informações detalhadas
- ✅ **Sistema de aprovação** por coordenadores
- ✅ **Inscrições online** com controle de vagas
- ✅ **Controle de presença** manual
- ✅ **Status automático** (Pendente → Aprovado → Em Andamento → Concluído)
- ✅ **Cancelamento** por organizadores ou administradores

### 🏢 Gestão de Espaços

- ✅ **Cadastro de salas** e espaços físicos
- ✅ **Controle de capacidade** e recursos
- ✅ **Verificação de conflitos** de horário
- ✅ **Status operacional** das salas
- ✅ **Inventário de equipamentos** por sala

### 📧 Sistema de Notificações

- ✅ **Email de boas-vindas** após cadastro
- ✅ **Verificação de email** automática
- ✅ **Recuperação de senha** segura
- ✅ **Notificações de eventos** (futuro)
- ✅ **Templates HTML** personalizados

### 📊 Relatórios e Controle

- ✅ **Auditoria completa** de ações
- ✅ **Timestamps automáticos** em todas as tabelas
- ✅ **Logs de segurança** e acesso
- ✅ **Controle de presença** em eventos
- ✅ **Histórico de participações**

---

## 🛠️ Tecnologias Utilizadas

### 🔧 Backend

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **Java** | 21 LTS | Linguagem de programação principal |
| **Spring Boot** | 3.3.8 | Framework principal |
| **Spring Data JPA** | 3.3.8 | Persistência de dados |
| **Spring Security** | 6.x | Segurança e autenticação |
| **Spring Validation** | 3.3.8 | Validação de dados |
| **JWT (JJWT)** | 0.12.5 | Tokens de autenticação |
| **PostgreSQL** | Latest | Banco de dados principal |
| **H2 Database** | Test | Banco de dados para testes |
| **Lombok** | Latest | Redução de boilerplate |
| **Maven** | 3.9+ | Gerenciador de dependências |

### 📚 Documentação

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **Swagger/OpenAPI** | 3.0 | Documentação interativa da API |
| **SpringDoc** | 2.2.0 | Integração Spring + Swagger |

### ✉️ Comunicação

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **Spring Mail** | 3.3.8 | Envio de emails |
| **Thymeleaf** | 3.1.x | Templates HTML para emails |

### 🧪 Testes e Qualidade

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **JUnit 5** | 5.10.x | Framework de testes |
| **Spring Boot Test** | 3.3.8 | Testes de integração |
| **JaCoCo** | 0.8.12 | Cobertura de código |
| **Spring Security Test** | 6.x | Testes de segurança |

### 🐳 DevOps

| Tecnologia | Versão | Descrição |
|-----------|--------|-----------|
| **Docker** | Latest | Containerização |
| **Docker Compose** | Latest | Orquestração de containers |
| **Spring Boot DevTools** | 3.3.8 | Hot reload em desenvolvimento |

---

## 📋 Pré-requisitos

### 🖥️ Software Necessário

- **Java 21** ou superior ([OpenJDK](https://openjdk.java.net/projects/jdk/21/))
- **Docker** e **Docker Compose** ([Instalação](https://docs.docker.com/get-docker/))
- **Git** ([Instalação](https://git-scm.com/downloads))
- **IDE** recomendada: IntelliJ IDEA, Eclipse ou VS Code

### 🔧 Ferramentas Opcionais

- **Maven** 3.9+ (ou usar o wrapper incluído `./mvnw`)
- **PostgreSQL** 15+ (se não usar Docker)
- **Postman** ou **Insomnia** para testar APIs

---

## 🔧 Instalação e Configuração

### 1️⃣ Clone o Repositório

```bash
git clone https://github.com/ifpb/ifmeetup.git
cd ifmeetup
```

### 2️⃣ Configure as Variáveis de Ambiente

Copie os arquivos de exemplo e configure conforme seu ambiente:

```bash
# Configuração de desenvolvimento
cp src/main/resources/application-dev-example.yml src/main/resources/application-dev.yml

# Configuração de produção (se necessário)
cp src/main/resources/application-example.yml src/main/resources/application-prod.yml
```

### 3️⃣ Configure o Banco de Dados

#### 🐳 Opção 1: Usando Docker (Recomendado)

```bash
# Inicia o PostgreSQL via Docker Compose
docker-compose up -d postgres
```

#### 🗄️ Opção 2: PostgreSQL Local

1. Instale o PostgreSQL
2. Crie o banco de dados:

```sql
CREATE DATABASE ifmeetup;
CREATE USER ifmeetup WITH PASSWORD 'ifmeetup123';
GRANT ALL PRIVILEGES ON DATABASE ifmeetup TO ifmeetup;
```

### 4️⃣ Configure o Email (Opcional)

Edite `application-dev.yml` com suas configurações de email:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: seu-email@gmail.com
    password: sua-senha-app
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## 🏃‍♂️ Como Executar

### 🚀 Execução Rápida

```bash
# Inicia banco de dados
docker-compose up -d

# Executa a aplicação
./mvnw spring-boot:run
```

### 🔧 Execução Detalhada

#### 1️⃣ Inicie o Banco de Dados

```bash
docker-compose up -d postgres
```

#### 2️⃣ Compile e Execute

```bash
# Compila o projeto
./mvnw clean compile

# Executa os testes
./mvnw test

# Inicia a aplicação
./mvnw spring-boot:run
```

#### 3️⃣ Acesse a Aplicação

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

### 🐳 Execução com Docker

```bash
# Construir a imagem
docker build -t ifmeetup .

# Executar com Docker Compose
docker-compose up
```

---

## 📖 Documentação da API

### 🌐 Swagger UI

Acesse a documentação interativa da API em:
**http://localhost:8080/swagger-ui.html**

### 🔗 Endpoints Principais

#### 🔐 Autenticação (`/auth`)

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `POST` | `/auth/register` | Registrar novo usuário | ✅ |
| `POST` | `/auth/login` | Fazer login | ✅ |
| `POST` | `/auth/suap/login` | **Login SUAP** (matrícula/senha IFPB) | ✅ |
| `POST` | `/auth/logout` | Fazer logout | ✅ |
| `GET` | `/auth/verify` | Verificar conta via email | ✅ |
| `POST` | `/auth/forgot-password` | Solicitar recuperação de senha | ✅ |
| `POST` | `/auth/reset-password` | Redefinir senha | ✅ |
| `GET` | `/auth/me` | Obter dados do usuário atual | ✅ |

#### 👥 Usuários (`/users`) - Em Desenvolvimento

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `GET` | `/users` | Listar usuários | 🚧 |
| `GET` | `/users/{id}` | Obter usuário por ID | 🚧 |
| `PUT` | `/users/{id}` | Atualizar usuário | 🚧 |
| `DELETE` | `/users/{id}` | Desativar usuário | 🚧 |

#### 📅 Eventos (`/events`) - Em Desenvolvimento

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `GET` | `/events` | Listar eventos | 🚧 |
| `POST` | `/events` | Criar evento | 🚧 |
| `GET` | `/events/{id}` | Obter evento por ID | 🚧 |
| `PUT` | `/events/{id}` | Atualizar evento | 🚧 |
| `DELETE` | `/events/{id}` | Cancelar evento | 🚧 |
| `POST` | `/events/{id}/register` | Inscrever-se no evento | 🚧 |
| `DELETE` | `/events/{id}/unregister` | Cancelar inscrição | 🚧 |

#### 🏢 Salas (`/rooms`) - Em Desenvolvimento

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `GET` | `/rooms` | Listar salas | 🚧 |
| `POST` | `/rooms` | Cadastrar sala | 🚧 |
| `GET` | `/rooms/{id}` | Obter sala por ID | 🚧 |
| `PUT` | `/rooms/{id}` | Atualizar sala | 🚧 |
| `DELETE` | `/rooms/{id}` | Desativar sala | 🚧 |

### 📝 Exemplos de Uso

#### 📋 Registro de Usuário

```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "Maria",
  "lastName": "Silva",
  "email": "maria.silva@exemplo.com",
  "phoneNumber": "(83) 99999-9999",
  "password": "senhaSegura123",
  "profileType": "STUDENT"
}
```

#### 🔑 Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "maria.silva@exemplo.com",
  "password": "senhaSegura123"
}
```

#### 🏫 Login SUAP (Credenciais Institucionais)

```http
POST /auth/suap/login
Content-Type: application/json

{
  "username": "1323726",
  "password": "minhasenhasuap123"
}
```

**Tipos de usuário SUAP:**
- **Servidores**: Matrícula de 7-12 dígitos (ex: `1323726`)
- **Alunos**: Matrícula completa (ex: `202215020007`)

**Mapeamento automático de perfis:**
- **STUDENT**: Todos os alunos
- **COORDINATOR**: Professores com função de coordenação ou cargos administrativos
- **TEACHER**: Professores sem função de coordenação e outros servidores

#### ✅ Verificação de Conta

```http
GET /auth/verify?token=seu-token-de-verificacao
```

---

## 🧪 Testes

### 🏃‍♂️ Executar Testes

```bash
# Todos os testes
./mvnw test

# Testes de integração
./mvnw test -Dtest=**/*IntegrationTest

# Testes unitários
./mvnw test -Dtest=**/*UnitTest
```

### 📊 Cobertura de Código

```bash
# Gerar relatório de cobertura
./mvnw clean test jacoco:report

# Ver relatório
open target/site/jacoco/index.html
```

### 🎯 Métricas de Qualidade

- **Cobertura mínima**: 60% linhas, 55% branches
- **Exclusões**: Entidades JPA, DTOs, configurações
- **Relatório**: `target/site/jacoco/index.html`

---

## 🚀 Deploy

### 🌐 Produção

1. **Configure as variáveis de ambiente**:

```bash
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/ifmeetup_prod
export DATABASE_USERNAME=ifmeetup_prod
export DATABASE_PASSWORD=senha_segura
export JWT_SECRET=chave_jwt_super_secreta
export MAIL_HOST=smtp.seuprovedor.com
export MAIL_USERNAME=noreply@seudominío.com
export MAIL_PASSWORD=senha_email
```

2. **Compile para produção**:

```bash
./mvnw clean package -Pprod
```

3. **Execute**:

```bash
java -jar target/ifmeetup-0.0.1-SNAPSHOT.jar
```

### 🐳 Docker em Produção

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/ifmeetup
    depends_on:
      - postgres
    restart: unless-stopped
      
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ifmeetup
      POSTGRES_USER: ifmeetup
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data_prod:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  postgres_data_prod:
```

---

## 🤝 Contribuindo

### 📋 Como Contribuir

1. **Fork** o projeto
2. **Crie** uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. **Push** para a branch (`git push origin feature/AmazingFeature`)
5. **Abra** um Pull Request

### 📝 Padrões de Código

- ✅ **Java Code Conventions**
- ✅ **Spring Boot Best Practices**
- ✅ **Clean Code** principles
- ✅ **SOLID** principles
- ✅ **Javadoc** para métodos públicos

### 🧪 Antes de Submeter

```bash
# Verifique formatação
./mvnw spotless:check

# Execute todos os testes
./mvnw clean test

# Verifique cobertura
./mvnw jacoco:check
```

---

## 📄 Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 👥 Equipe

### 🏫 Instituto Federal da Paraíba (IFPB)

Este projeto foi desenvolvido como parte das atividades acadêmicas do IFPB, visando modernizar e centralizar o gerenciamento de eventos da instituição.

### 🤝 Contato

- **Email**: contato@ifpb.edu.br
- **Site**: [https://www.ifpb.edu.br](https://www.ifpb.edu.br)
- **GitHub**: [https://github.com/ifpb](https://github.com/ifpb)

---

## 🙏 Agradecimentos

- **Comunidade Spring Boot** pelos excelentes recursos e documentação
- **PostgreSQL** pela robustez e confiabilidade
- **Docker** pela facilidade de deployment
- **Swagger** pela documentação automática da API
- **Instituto Federal da Paraíba** pelo apoio institucional

---

<div align="center">

**⭐ Se este projeto foi útil para você, considere dar uma estrela!**

**🚀 Desenvolvido com ❤️ para a comunidade acadêmica do IFPB**

</div>
