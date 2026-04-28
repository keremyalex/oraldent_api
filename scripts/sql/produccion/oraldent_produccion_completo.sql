begin;

-- Script maestro para PostgreSQL.
-- Crea la estructura base de Oraldent y carga catálogos/horarios iniciales.

create table if not exists pacientes (
    id bigserial primary key,
    nombre varchar(80) not null,
    apellido_paterno varchar(80) not null,
    apellido_materno varchar(80),
    celular varchar(30) not null,
    documento_identidad varchar(30),
    correo varchar(120),
    fecha_nacimiento date,
    direccion varchar(200),
    foto_url varchar(500),
    foto_public_id varchar(255),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now()
);

create table if not exists servicios (
    id bigserial primary key,
    nombre varchar(150) not null,
    descripcion varchar(500),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint uk_servicios_nombre unique (nombre)
);

create table if not exists horarios (
    id bigserial primary key,
    dia_semana varchar(20) not null,
    hora_inicio time not null,
    hora_fin time not null,
    duracion_cita_minutos integer not null,
    observacion varchar(200),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint ck_horarios_dia_semana check (
        dia_semana in ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO')
    ),
    constraint ck_horarios_duracion check (duracion_cita_minutos between 10 and 240),
    constraint ck_horarios_rango check (hora_inicio < hora_fin)
);

comment on table horarios is 'Cada fila representa un bloque horario. Esto permite varios turnos por el mismo día.';

create table if not exists usuarios (
    id bigserial primary key,
    nombre varchar(80) not null,
    apellido_paterno varchar(80) not null,
    apellido_materno varchar(80),
    correo varchar(120),
    celular varchar(30) not null,
    password varchar(255) not null,
    rol varchar(20) not null,
    activo boolean not null default true,
    verificado boolean not null default true,
    foto_perfil_url varchar(500),
    foto_perfil_public_id varchar(255),
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint uk_usuarios_correo unique (correo),
    constraint uk_usuarios_celular unique (celular),
    constraint ck_usuarios_rol check (
        rol in ('ADMIN', 'DOCTOR', 'RECEPCION', 'PACIENTE')
    )
);

create table if not exists citas (
    id bigserial primary key,
    paciente_id bigint not null,
    usuario_id bigint,
    servicio_id bigint,
    fecha_hora_inicio timestamp without time zone not null,
    fecha_hora_fin timestamp without time zone not null,
    motivo varchar(300) not null,
    estado varchar(20) not null default 'PENDIENTE',
    codigo_gestion varchar(60) not null,
    notas varchar(500),
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_citas_paciente foreign key (paciente_id) references pacientes (id),
    constraint fk_citas_usuario foreign key (usuario_id) references usuarios (id),
    constraint fk_citas_servicio foreign key (servicio_id) references servicios (id),
    constraint uk_citas_codigo_gestion unique (codigo_gestion),
    constraint ck_citas_estado check (
        estado in ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'REPROGRAMADA', 'ATENDIDA', 'NO_ASISTIO')
    ),
    constraint ck_citas_rango check (fecha_hora_inicio < fecha_hora_fin)
);

create index if not exists idx_pacientes_celular on pacientes (celular);
create index if not exists idx_pacientes_correo on pacientes (correo);
create index if not exists idx_citas_paciente_id on citas (paciente_id);
create index if not exists idx_citas_usuario_id on citas (usuario_id);
create index if not exists idx_citas_servicio_id on citas (servicio_id);
create index if not exists idx_citas_fecha_hora_inicio on citas (fecha_hora_inicio);
create index if not exists idx_horarios_dia_semana on horarios (dia_semana);

insert into servicios (nombre, descripcion, activo)
values
    ('Implantologia Dental', 'Tratamientos relacionados con implantes dentales.', true),
    ('Rayo X', 'Toma y apoyo diagnostico radiografico.', true),
    ('Blanqueamiento Dental Laser', 'Tratamiento estetico para aclaramiento dental.', true),
    ('Ortodoncia', 'Correccion de alineacion y mordida.', true),
    ('Protesis Fija y Removible', 'Rehabilitacion oral con protesis parciales o completas.', true)
on conflict (nombre) do update
set descripcion = excluded.descripcion,
    activo = true,
    fecha_actualizacion = now();

delete from horarios
where dia_semana in ('LUNES', 'MARTES', 'MIERCOLES', 'JUEVES', 'VIERNES', 'SABADO', 'DOMINGO');

insert into horarios (
    dia_semana,
    hora_inicio,
    hora_fin,
    duracion_cita_minutos,
    observacion,
    activo
)
values
    ('LUNES', '08:30:00', '12:30:00', 30, 'Turno mañana', true),
    ('LUNES', '15:00:00', '22:30:00', 30, 'Turno tarde', true),
    ('MARTES', '08:30:00', '12:30:00', 30, 'Turno mañana', true),
    ('MARTES', '15:00:00', '22:30:00', 30, 'Turno tarde', true),
    ('MIERCOLES', '08:30:00', '12:30:00', 30, 'Turno mañana', true),
    ('MIERCOLES', '15:00:00', '22:30:00', 30, 'Turno tarde', true),
    ('JUEVES', '08:30:00', '12:30:00', 30, 'Turno mañana', true),
    ('JUEVES', '15:00:00', '22:30:00', 30, 'Turno tarde', true),
    ('VIERNES', '08:30:00', '12:30:00', 30, 'Turno mañana', true),
    ('VIERNES', '15:00:00', '22:30:00', 30, 'Turno tarde', true),
    ('SABADO', '09:00:00', '22:30:00', 30, 'Turno continuo', true);

-- Datos iniciales
-- Usuario administrador inicial
-- password plano: admin1234
insert into usuarios (
    nombre,
    apellido_paterno,
    apellido_materno,
    correo,
    celular,
    password,
    rol,
    activo,
    verificado,
    foto_perfil_url,
    foto_perfil_public_id
)
values (
    'Valeria',
    'Soria',
    'Lopez',
    'admin@oraldent.com',
    '70000001',
    '$2a$10$RsNBQ6h8ktZ8s..fLOXJHesEBEONj8KsnZDrHn.DPuMdFIUucNG0q',
    'ADMIN',
    true,
    true,
    null,
    null
)
on conflict (correo) do update
set nombre = excluded.nombre,
    apellido_paterno = excluded.apellido_paterno,
    apellido_materno = excluded.apellido_materno,
    celular = excluded.celular,
    password = excluded.password,
    rol = excluded.rol,
    activo = true,
    verificado = true,
    foto_perfil_url = excluded.foto_perfil_url,
    foto_perfil_public_id = excluded.foto_perfil_public_id,
    fecha_actualizacion = now();

delete from citas
where codigo_gestion in (
    'ORALA001',
    'ORALA002',
    'ORALA003',
    'ORALA004',
    'ORALA005',
    'ORALA006'
);

delete from pacientes
where celular in ('70010001', '70010002', '70010003');

insert into pacientes (
    nombre,
    apellido_paterno,
    apellido_materno,
    celular,
    documento_identidad,
    correo,
    fecha_nacimiento,
    direccion,
    foto_url,
    foto_public_id,
    activo
)
values
    ('Maria Fernanda', 'Rojas', 'Flores', '70010001', '1234567', 'maria@oraldent.com', '1997-03-12', 'Zona Central', null, null, true),
    ('Luis Alberto', 'Mamani', 'Quispe', '70010002', '2345678', 'luis@oraldent.com', '1992-08-21', 'Av. Busch', null, null, true),
    ('Camila', 'Perez', 'Vargas', '70010003', '3456789', 'camila@oraldent.com', '2001-01-05', 'Zona Norte', null, null, true);

insert into citas (
    paciente_id,
    usuario_id,
    servicio_id,
    fecha_hora_inicio,
    fecha_hora_fin,
    motivo,
    estado,
    codigo_gestion,
    notas
)
values
    (
        (select id from pacientes where celular = '70010001' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from servicios where nombre = 'Implantologia Dental' limit 1),
        '2026-04-27 09:00:00',
        '2026-04-27 09:30:00',
        'Valoracion inicial para implante',
        'CONFIRMADA',
        'ORALA001',
        'Primera valoracion de la semana.'
    ),
    (
        (select id from pacientes where celular = '70010002' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from servicios where nombre = 'Rayo X' limit 1),
        '2026-04-27 15:30:00',
        '2026-04-27 16:00:00',
        'Radiografia panoramica de control',
        'PENDIENTE',
        'ORALA002',
        'Paciente referido para apoyo diagnostico.'
    ),
    (
        (select id from pacientes where celular = '70010003' limit 1),
        null,
        (select id from servicios where nombre = 'Ortodoncia' limit 1),
        '2026-04-28 10:00:00',
        '2026-04-28 10:30:00',
        'Control de ortodoncia',
        'CONFIRMADA',
        'ORALA003',
        'Ajuste mensual.'
    ),
    (
        (select id from pacientes where celular = '70010001' limit 1),
        null,
        (select id from servicios where nombre = 'Blanqueamiento Dental Laser' limit 1),
        '2026-04-29 16:00:00',
        '2026-04-29 16:30:00',
        'Evaluacion para blanqueamiento',
        'PENDIENTE',
        'ORALA004',
        'Paciente solicita tratamiento estetico.'
    ),
    (
        (select id from pacientes where celular = '70010002' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from servicios where nombre = 'Protesis Fija y Removible' limit 1),
        '2026-05-01 11:00:00',
        '2026-05-01 11:30:00',
        'Revision de protesis',
        'REPROGRAMADA',
        'ORALA005',
        'Cita reprogramada por disponibilidad del paciente.'
    ),
    (
        (select id from pacientes where celular = '70010003' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from servicios where nombre = 'Rayo X' limit 1),
        '2026-05-02 09:00:00',
        '2026-05-02 09:30:00',
        'Rayo X de seguimiento',
        'CONFIRMADA',
        'ORALA006',
        'Control sabatino.'
    );

commit;
