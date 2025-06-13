# Modelo Lógico do Banco de Dados - Sistema IFMeetup

## Visão Geral
Este documento descreve o modelo lógico do banco de dados do Sistema IFMeetup para gerenciamento de eventos acadêmicos, utilizando PostgreSQL como SGBD.

## Características do Banco
- **SGBD**: PostgreSQL
- **Chaves Primárias**: UUID
- **Auditoria**: Timestamps automáticos em todas as tabelas

---

## 📋 ESTRUTURA DAS TABELAS

### 1. **users** (Usuários do Sistema)
Tabela principal para armazenamento de usuários do sistema.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **email** | VARCHAR(255) | NOT NULL, UNIQUE | Email único para login |
| **password** | VARCHAR(255) | NOT NULL | Senha criptografada |
| **first_name** | VARCHAR(50) | NOT NULL, CHECK (LENGTH >= 2) | Primeiro nome |
| **last_name** | VARCHAR(50) | NOT NULL, CHECK (LENGTH >= 2) | Sobrenome |
| **phone_number** | VARCHAR(255) | NOT NULL, UNIQUE | Número de telefone único |
| **email_verified** | BOOLEAN | NOT NULL, DEFAULT FALSE | Status de verificação de email |
| **account_non_expired** | BOOLEAN | NOT NULL, DEFAULT TRUE | Indica se conta não está expirada |
| **account_non_locked** | BOOLEAN | NOT NULL, DEFAULT TRUE | Indica se conta não está bloqueada |
| **credentials_non_expired** | BOOLEAN | NOT NULL, DEFAULT TRUE | Indica se credenciais não expiraram |
| **last_login** | TIMESTAMP WITH TIME ZONE | NULL | Último acesso registrado |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Índices:**
- `idx_users_email` UNIQUE (email)
- `idx_users_phone` UNIQUE (phone_number)
- `idx_users_last_login` (last_login) -- Para relatórios de atividade

**Triggers de Auditoria:**
- `trg_users_updated_at` - Atualiza `updated_at` automaticamente

**Comentários:**
```sql
COMMENT ON TABLE users IS 'Usuários do sistema com dados de autenticação';
COMMENT ON COLUMN users.password IS 'Senha hash BCrypt - nunca armazenar texto puro';
COMMENT ON COLUMN users.last_login IS 'Usado para relatórios de atividade e limpeza de contas inativas';
```

---

### 2. **user_profiles** (Perfis Estendidos de Usuário)
Informações adicionais e configurações de perfil dos usuários.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **user_id** | UUID | FOREIGN KEY (users.id), NOT NULL, UNIQUE | Referência ao usuário |
| **profile_type** | VARCHAR(20) | NOT NULL, CHECK (profile_type IN ('ADMIN','COORDINATOR','TEACHER','STUDENT')) | Tipo de perfil |
| **profile_picture_url** | VARCHAR(255) | NULL | URL da foto de perfil |
| **verified** | BOOLEAN | NOT NULL, DEFAULT FALSE | Perfil verificado institucionalmente |
| **verification_date** | TIMESTAMP WITH TIME ZONE | NULL | Data da verificação institucional |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Relacionamentos:**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

**Comentários:**
```sql
COMMENT ON TABLE user_profiles IS 'Perfis estendidos com informações institucionais';
COMMENT ON COLUMN user_profiles.verified IS 'Verificação manual por coordenadores/admins';
COMMENT ON COLUMN user_profiles.profile_type IS 'Determina nível de acesso básico no sistema';
```

---

### 3. **roles** (Papéis de Acesso)
Tabela de papéis para sistema de controle de acesso baseado em funções (RBAC).

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **name** | VARCHAR(50) | NOT NULL, UNIQUE | Nome único do papel |
| **description** | VARCHAR(255) | NULL | Descrição do papel |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Dados de Sistema (Pré-carregados):**
```sql
INSERT INTO roles (id, name, description) VALUES 
('19a8f7e6-d5c4-4b3a-8291-f0e9d8c7b6a5', 'ADMIN', 'Administrador do sistema'),
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'COORDINATOR', 'Coordenador de eventos'),
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'TEACHER', 'Professor'),
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'STUDENT', 'Estudante');
```

---

### 4. **permissions** (Permissões Específicas)
Tabela de permissões granulares do sistema.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **name** | VARCHAR(100) | NOT NULL, UNIQUE | Nome único da permissão |
| **description** | VARCHAR(255) | NULL | Descrição da permissão |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Categorias de Permissões:**
- **Administração**: ADMIN_ACCESS, USER_CREATE, USER_VIEW, USER_EDIT, USER_DELETE
- **Eventos**: EVENT_CREATE, EVENT_VIEW_ALL, EVENT_VIEW_PUBLISHED, EVENT_EDIT, EVENT_DELETE, EVENT_APPROVE
- **Salas**: ROOM_RESERVE, ROOM_VIEW_ALL, ROOM_VIEW_AVAILABILITY, ROOM_MANAGE_RESERVATIONS
- **Participação**: EVENT_REGISTER_SELF, EVENT_VIEW_PARTICIPANTS, EVENT_MANAGE_PARTICIPANTS

---

### 5. **user_roles** (Associação Usuário-Papel)
Tabela de junção para relacionamento many-to-many entre usuários e papéis.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **user_id** | UUID | FOREIGN KEY (users.id), NOT NULL | Referência ao usuário |
| **role_id** | UUID | FOREIGN KEY (roles.id), NOT NULL | Referência ao papel |

**Constraints:**
- PRIMARY KEY (user_id, role_id)
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
- FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT

**Comentários:**
```sql
COMMENT ON TABLE user_roles IS 'Associação N:N entre usuários e papéis - um usuário pode ter múltiplos papéis';
```

---

### 6. **role_permissions** (Associação Papel-Permissão)
Tabela de junção para relacionamento many-to-many entre papéis e permissões.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **role_id** | UUID | FOREIGN KEY (roles.id), NOT NULL | Referência ao papel |
| **permission_id** | UUID | FOREIGN KEY (permissions.id), NOT NULL | Referência à permissão |

**Constraints:**
- PRIMARY KEY (role_id, permission_id)
- FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
- FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE RESTRICT

---

### 7. **rooms** (Salas e Espaços Físicos)
Cadastro de salas/espaços disponíveis para eventos.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **name** | VARCHAR(100) | NOT NULL, CHECK (LENGTH >= 3) | Nome da sala |
| **location** | VARCHAR(255) | NULL | Localização detalhada |
| **capacity** | INTEGER | NOT NULL, CHECK (capacity > 0) | Capacidade máxima de pessoas |
| **type** | VARCHAR(30) | NOT NULL, CHECK (type IN ('CLASSROOM','AUDITORIUM','LABORATORY','MEETING_ROOM','SHARED_SPACE','OTHER')) | Tipo da sala |
| **status** | VARCHAR(30) | NOT NULL, CHECK (status IN ('AVAILABLE','UNAVAILABLE','UNDER_MAINTENANCE','DISABLED')) | Status operacional |
| **description** | TEXT | NULL, CHECK (LENGTH(description) <= 1000) | Descrição detalhada |
| **created_by** | UUID | FOREIGN KEY (users.id), NOT NULL | Usuário que cadastrou |
| **updated_by** | UUID | FOREIGN KEY (users.id), NULL | Último usuário que alterou |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Relacionamentos:**
- FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
- FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL

**Índices:**
- `idx_rooms_status` (status) -- Para consultas de disponibilidade
- `idx_rooms_type` (type) -- Para filtros por tipo
- `idx_rooms_capacity` (capacity) -- Para busca por capacidade

**Comentários:**
```sql
COMMENT ON TABLE rooms IS 'Cadastro de salas físicas e espaços para eventos';
COMMENT ON COLUMN rooms.status IS 'AVAILABLE=Disponível, UNAVAILABLE=Temporariamente indisponível, UNDER_MAINTENANCE=Em manutenção, DISABLED=Desabilitada';
COMMENT ON COLUMN rooms.created_by IS 'Auditoria: usuário responsável pelo cadastro';
COMMENT ON COLUMN rooms.updated_by IS 'Auditoria: último usuário que modificou os dados';
```

---

### 8. **room_inventory_resources** (Inventário de Recursos)
Recursos e equipamentos disponíveis em cada sala.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **room_id** | UUID | FOREIGN KEY (rooms.id), NOT NULL | Referência à sala |
| **resource_type** | VARCHAR(50) | NOT NULL, CHECK (resource_type IN ('PROJECTOR','COMPUTER','WHITEBOARD','SOUND_SYSTEM','AIR_CONDITIONING','PRINTER','VIDEO_CONFERENCE_SYSTEM','OTHER')) | Tipo do recurso |
| **quantity** | INTEGER | NOT NULL, CHECK (quantity >= 0) | Quantidade disponível |
| **details** | VARCHAR(255) | NULL | Detalhes específicos do recurso |

**Constraints:**
- UNIQUE (room_id, resource_type) -- Evita duplicação de recursos por sala
- FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE

**Comentários:**
```sql
COMMENT ON TABLE room_inventory_resources IS 'Inventário de equipamentos e recursos por sala';
COMMENT ON COLUMN room_inventory_resources.quantity IS 'Quantidade total do recurso - 0 indica indisponível';
```

---

### 9. **events** (Eventos do Sistema)
Registro de todos os eventos organizados no sistema.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **title** | VARCHAR(150) | NOT NULL, CHECK (LENGTH >= 5) | Título do evento |
| **description** | TEXT | NOT NULL | Descrição detalhada |
| **organizer_user_id** | UUID | FOREIGN KEY (users.id), NOT NULL | Organizador responsável |
| **room_id** | UUID | FOREIGN KEY (rooms.id), NOT NULL | Sala reservada |
| **start_date_time** | TIMESTAMP WITH TIME ZONE | NOT NULL, CHECK (start_date_time > CURRENT_TIMESTAMP) | Início do evento |
| **end_date_time** | TIMESTAMP WITH TIME ZONE | NOT NULL, CHECK (end_date_time > start_date_time) | Término do evento |
| **max_participants** | INTEGER | NOT NULL, CHECK (max_participants > 0) | Limite de participantes |
| **status** | VARCHAR(30) | NOT NULL, CHECK (status IN ('PENDING_APPROVAL','APPROVED','REJECTED','CANCELED_BY_ORGANIZER','CANCELED_BY_ADMIN','CONCLUDED','IN_PROGRESS')) | Status do evento |
| **event_type** | VARCHAR(30) | NOT NULL, CHECK (event_type IN ('COURSE','WORKSHOP','LECTURE','MEETING','SEMINAR','MINICOURSE','OTHER')) | Categoria do evento |
| **public_event** | BOOLEAN | NOT NULL, DEFAULT TRUE | Evento aberto ao público |
| **approved_by_user_id** | UUID | FOREIGN KEY (users.id), NULL | Usuário que aprovou |
| **approval_date_time** | TIMESTAMP WITH TIME ZONE | NULL | Data/hora da aprovação |
| **rejection_reason** | TEXT | NULL | Motivo da rejeição |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Relacionamentos:**
- FOREIGN KEY (organizer_user_id) REFERENCES users(id) ON DELETE RESTRICT
- FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
- FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT

**Índices Compostos:**
- `idx_events_room_datetime` (room_id, start_date_time, end_date_time) -- Para detecção de conflitos
- `idx_events_status_start` (status, start_date_time) -- Para listagens por status
- `idx_events_organizer` (organizer_user_id) -- Para eventos por organizador

**Constraints Complexas:**
```sql
-- Constraint para verificar sobreposição de horários na mesma sala
CREATE OR REPLACE FUNCTION check_room_conflict() RETURNS TRIGGER AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM events 
        WHERE room_id = NEW.room_id 
        AND id != COALESCE(NEW.id, '00000000-0000-0000-0000-000000000000'::uuid)
        AND status NOT IN ('REJECTED', 'CANCELED_BY_ADMIN', 'CANCELED_BY_ORGANIZER')
        AND (start_date_time < NEW.end_date_time AND end_date_time > NEW.start_date_time)
    ) THEN
        RAISE EXCEPTION 'Conflito de horário: sala já reservada no período especificado';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_room_conflict
    BEFORE INSERT OR UPDATE ON events
    FOR EACH ROW EXECUTE FUNCTION check_room_conflict();
```

**Comentários:**
```sql
COMMENT ON TABLE events IS 'Eventos organizados no sistema - base para todo agendamento';
COMMENT ON COLUMN events.organizer_user_id IS 'Responsável pelo evento - não pode ser alterado após criação';
COMMENT ON COLUMN events.approved_by_user_id IS 'Auditoria: quem aprovou o evento (apenas COORDINATOR+)';
COMMENT ON COLUMN events.status IS 'Fluxo: PENDING_APPROVAL -> APPROVED -> IN_PROGRESS -> CONCLUDED';
```

---

### 10. **event_participants** (Participantes dos Eventos)
Registro de inscrições e participação em eventos.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **event_id** | UUID | FOREIGN KEY (events.id), NOT NULL | Evento referenciado |
| **user_id** | UUID | FOREIGN KEY (users.id), NOT NULL | Participante |
| **registration_date_time** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da inscrição |
| **attendance_status** | VARCHAR(30) | NOT NULL, CHECK (attendance_status IN ('REGISTERED','PRESENT','ABSENT','CANCELED')) | Status de participação |
| **certificate_issued** | BOOLEAN | NOT NULL, DEFAULT FALSE | Certificado foi emitido |
| **feedback** | TEXT | NULL | Avaliação do participante |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Constraints:**
- UNIQUE (event_id, user_id) -- Evita inscrição duplicada
- FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

**Índices:**
- `idx_participants_event` (event_id) -- Para listar participantes
- `idx_participants_user` (user_id) -- Para histórico do usuário
- `idx_participants_status` (attendance_status) -- Para relatórios

**Triggers de Validação:**
```sql
-- Verificar capacidade máxima do evento
CREATE OR REPLACE FUNCTION check_event_capacity() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.attendance_status = 'REGISTERED' THEN
        IF (SELECT COUNT(*) FROM event_participants ep 
            JOIN events e ON ep.event_id = e.id 
            WHERE ep.event_id = NEW.event_id 
            AND ep.attendance_status = 'REGISTERED') >= 
           (SELECT max_participants FROM events WHERE id = NEW.event_id) THEN
            RAISE EXCEPTION 'Evento lotado: capacidade máxima atingida';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**Comentários:**
```sql
COMMENT ON TABLE event_participants IS 'Inscrições e controle de presença em eventos';
COMMENT ON COLUMN event_participants.registration_date_time IS 'Timestamp da inscrição - imutável após criação';
COMMENT ON COLUMN event_participants.attendance_status IS 'REGISTERED -> PRESENT/ABSENT (controlado manualmente)';
COMMENT ON COLUMN event_participants.certificate_issued IS 'Flag para controle de emissão de certificados';
```

---

### 11. **verification_tokens** (Tokens de Verificação)
Tokens temporários para verificação de email.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **token** | VARCHAR(255) | NOT NULL, UNIQUE | Token de verificação |
| **user_id** | UUID | FOREIGN KEY (users.id), NOT NULL | Usuário associado |
| **expiry_date** | TIMESTAMP WITH TIME ZONE | NOT NULL | Data de expiração |
| **used** | BOOLEAN | NOT NULL, DEFAULT FALSE | Token já utilizado |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Relacionamentos:**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

**Índices:**
- `idx_verification_token` UNIQUE (token)
- `idx_verification_expiry` (expiry_date) -- Para limpeza de tokens expirados

**Comentários:**
```sql
COMMENT ON TABLE verification_tokens IS 'Tokens temporários para verificação de email - limpeza automática recomendada';
COMMENT ON COLUMN verification_tokens.expiry_date IS 'Tokens expiram em 24h por padrão';
```

---

### 12. **password_reset_tokens** (Tokens de Reset de Senha)
Tokens temporários para recuperação de senha.

| Campo | Tipo | Restrições | Descrição |
|-------|------|------------|-----------|
| **id** | UUID | PRIMARY KEY, NOT NULL | Identificador único |
| **token** | VARCHAR(100) | NOT NULL, UNIQUE | Token de reset |
| **user_id** | UUID | FOREIGN KEY (users.id), NOT NULL | Usuário associado |
| **expiry_date** | TIMESTAMP WITH TIME ZONE | NOT NULL | Data de expiração |
| **used** | BOOLEAN | NOT NULL, DEFAULT FALSE | Token já utilizado |
| **created_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora de criação |
| **updated_at** | TIMESTAMP WITH TIME ZONE | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Data/hora da última atualização |

**Relacionamentos:**
- FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE

**Políticas de Segurança:**
```sql
-- Token válido por apenas 1 hora
ALTER TABLE password_reset_tokens 
ADD CONSTRAINT chk_token_expiry 
CHECK (expiry_date <= created_at + INTERVAL '1 hour');
```

---

## 🔗 RELACIONAMENTOS E CARDINALIDADES

### **Hierarquia de Usuários**
```
users (1) ←→ (1) user_profiles
users (N) ←→ (N) roles [via user_roles]
roles (N) ←→ (N) permissions [via role_permissions]
```

### **Gestão de Infraestrutura**
```
users (1) ←→ (N) rooms [created_by]
users (1) ←→ (N) rooms [updated_by]
rooms (1) ←→ (N) room_inventory_resources
```

### **Gestão de Eventos**
```
users (1) ←→ (N) events [organizer]
users (0,1) ←→ (N) events [approved_by]
rooms (1) ←→ (N) events
events (1) ←→ (N) event_participants
users (1) ←→ (N) event_participants
```

### **Tokens de Segurança**
```
users (1) ←→ (0,N) verification_tokens
users (1) ←→ (0,N) password_reset_tokens
```

---