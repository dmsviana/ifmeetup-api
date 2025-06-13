-- Limpar dados existentes para evitar duplicação
TRUNCATE TABLE role_permissions CASCADE;
TRUNCATE TABLE roles CASCADE;
TRUNCATE TABLE permissions CASCADE;

-- Inserir Permissões Básicas do Sistema
INSERT INTO permissions (id, name, description, created_at, updated_at) VALUES
-- Permissões de Administração de Usuários e Sistema
('b76b5721-0c42-484a-9739-9f5a97a43d93', 'ADMIN_ACCESS', 'Acesso completo ao painel administrativo e funcionalidades de administrador', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c9b50209-e3a4-4a07-95d9-3c3a5b0b2e1d', 'USER_CREATE', 'Permite criar novos usuários no sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d3f62e2f-a5af-4dd0-9f4e-31b0c24213a4', 'USER_VIEW', 'Permite visualizar informações de usuários do sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('97e4c6e3-0c6d-4f6a-b104-7a78a75b3f15', 'USER_EDIT', 'Permite editar informações de usuários do sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('82e5c4a2-d889-4c95-a0b6-1959e540a9e5', 'USER_DELETE', 'Permite excluir usuários do sistema', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Permissões de Evento
('fad68e12-2a1a-40a4-9c2f-3b57a3d9fb3a', 'EVENT_CREATE', 'Permite criar novos eventos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e4adfc3e-6a0d-4018-8df2-85b89a6e2db2', 'EVENT_VIEW_ALL', 'Permite visualizar todos os eventos (publicados e pendentes)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a1b2c3d4-e5f6-7890-1234-567890abcdef', 'EVENT_VIEW_PUBLISHED', 'Permite visualizar apenas eventos publicados', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('7fb6c7a8-6b98-4c2a-bde1-d1e2a14ba9c4', 'EVENT_EDIT', 'Permite editar eventos (lógica de propriedade aplicada no serviço)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('6c4a0da9-3851-444a-a3f5-8b3dc4cb4ba7', 'EVENT_DELETE', 'Permite excluir eventos (lógica de propriedade aplicada no serviço)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('5d7bbbe6-c2be-4339-a38f-5f9dd89b8e1c', 'EVENT_APPROVE', 'Permite aprovar ou rejeitar eventos pendentes', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Permissões de Reserva de Sala
('3c1e4f2d-b7a8-43c9-9d6e-1b5c2a8f7e3a', 'ROOM_RESERVE', 'Permite solicitar a reserva de salas/espaços', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('2a9b7c8d-f5e4-40a1-b2c3-e9d8f7a6b5c4', 'ROOM_VIEW_ALL', 'Permite visualizar todas as reservas de salas e sua disponibilidade', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('b2c3d4e5-f6a7-8901-2345-678901abcdef', 'ROOM_VIEW_AVAILABILITY', 'Permite visualizar apenas a disponibilidade de salas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('1e2d3c4b-a5b6-47c8-9d0e-f1e2d3c4b5a6', 'ROOM_MANAGE_RESERVATIONS', 'Permite gerenciar (aprovar, rejeitar, cancelar) todas as reservas de salas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Permissões de Inscrição em Eventos (Participante)
('f1e2d3c4-b5a6-47c8-9d0e-1e2d3c4b5a67', 'EVENT_REGISTER_SELF', 'Permite que o usuário se inscreva em eventos', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('e2d3c4b5-a6f1-47c8-9d0e-1e2d3c4b5a68', 'EVENT_VIEW_PARTICIPANTS', 'Permite visualizar a lista de participantes de um evento', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('d3c4b5a6-f1e2-47c8-9d0e-1e2d3c4b5a69', 'EVENT_MANAGE_PARTICIPANTS', 'Permite gerenciar inscrições de participantes em eventos (ex: remover participante)', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Inserir Roles (Papéis)
INSERT INTO roles (id, name, description, created_at, updated_at) VALUES
('19a8f7e6-d5c4-4b3a-8291-f0e9d8c7b6a5', 'ADMIN', 'Administrador do sistema com acesso total', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'COORDINATOR', 'Coordenador de eventos, aprova eventos e reservas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'TEACHER', 'Professor, pode criar e gerenciar seus eventos e solicitar reservas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'STUDENT', 'Estudante, pode criar eventos (ex: SEMITI), participar de eventos e solicitar reservas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Atribuir Permissões às Roles

-- ADMIN: Todas as permissões
INSERT INTO role_permissions (role_id, permission_id)
SELECT '19a8f7e6-d5c4-4b3a-8291-f0e9d8c7b6a5', id FROM permissions;

-- COORDINATOR:
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Gerenciamento de Usuários (Visualização)
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'd3f62e2f-a5af-4dd0-9f4e-31b0c24213a4'), -- USER_VIEW
-- Eventos
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'fad68e12-2a1a-40a4-9c2f-3b57a3d9fb3a'), -- EVENT_CREATE
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'e4adfc3e-6a0d-4018-8df2-85b89a6e2db2'), -- EVENT_VIEW_ALL
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '7fb6c7a8-6b98-4c2a-bde1-d1e2a14ba9c4'), -- EVENT_EDIT (todos)
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '6c4a0da9-3851-444a-a3f5-8b3dc4cb4ba7'), -- EVENT_DELETE (todos)
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '5d7bbbe6-c2be-4339-a38f-5f9dd89b8e1c'), -- EVENT_APPROVE
-- Salas
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '3c1e4f2d-b7a8-43c9-9d6e-1b5c2a8f7e3a'), -- ROOM_RESERVE
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '2a9b7c8d-f5e4-40a1-b2c3-e9d8f7a6b5c4'), -- ROOM_VIEW_ALL
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', '1e2d3c4b-a5b6-47c8-9d0e-f1e2d3c4b5a6'), -- ROOM_MANAGE_RESERVATIONS
-- Participantes
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'f1e2d3c4-b5a6-47c8-9d0e-1e2d3c4b5a67'), -- EVENT_REGISTER_SELF (Coordenador também pode querer participar)
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'e2d3c4b5-a6f1-47c8-9d0e-1e2d3c4b5a68'), -- EVENT_VIEW_PARTICIPANTS
('28b9e7d6-c5a4-4b3a-9182-e1f8d7c6b5a4', 'd3c4b5a6-f1e2-47c8-9d0e-1e2d3c4b5a69'); -- EVENT_MANAGE_PARTICIPANTS

-- TEACHER:
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Eventos
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'fad68e12-2a1a-40a4-9c2f-3b57a3d9fb3a'), -- EVENT_CREATE
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'a1b2c3d4-e5f6-7890-1234-567890abcdef'), -- EVENT_VIEW_PUBLISHED (e os seus pendentes, via lógica de serviço)
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', '7fb6c7a8-6b98-4c2a-bde1-d1e2a14ba9c4'), -- EVENT_EDIT (para seus próprios eventos)
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', '6c4a0da9-3851-444a-a3f5-8b3dc4cb4ba7'), -- EVENT_DELETE (para seus próprios eventos)
-- Salas
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', '3c1e4f2d-b7a8-43c9-9d6e-1b5c2a8f7e3a'), -- ROOM_RESERVE
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'b2c3d4e5-f6a7-8901-2345-678901abcdef'), -- ROOM_VIEW_AVAILABILITY
-- Participantes
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'f1e2d3c4-b5a6-47c8-9d0e-1e2d3c4b5a67'), -- EVENT_REGISTER_SELF
('37c8d7e6-b5a4-4c3b-8273-d2e7f6c5b4a3', 'e2d3c4b5-a6f1-47c8-9d0e-1e2d3c4b5a68'); -- EVENT_VIEW_PARTICIPANTS (para seus próprios eventos)

-- STUDENT:
INSERT INTO role_permissions (role_id, permission_id) VALUES
-- Eventos
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'fad68e12-2a1a-40a4-9c2f-3b57a3d9fb3a'), -- EVENT_CREATE
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'a1b2c3d4-e5f6-7890-1234-567890abcdef'), -- EVENT_VIEW_PUBLISHED (e os seus pendentes, via lógica de serviço)
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', '7fb6c7a8-6b98-4c2a-bde1-d1e2a14ba9c4'), -- EVENT_EDIT (para seus próprios eventos)
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', '6c4a0da9-3851-444a-a3f5-8b3dc4cb4ba7'), -- EVENT_DELETE (para seus próprios eventos)
-- Salas
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', '3c1e4f2d-b7a8-43c9-9d6e-1b5c2a8f7e3a'), -- ROOM_RESERVE [cite: 19]
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'b2c3d4e5-f6a7-8901-2345-678901abcdef'), -- ROOM_VIEW_AVAILABILITY
-- Participantes
('46d7c6e5-a4b3-4d2c-7364-c3f6e5d4b3a2', 'f1e2d3c4-b5a6-47c8-9d0e-1e2d3c4b5a67'); -- EVENT_REGISTER_SELF