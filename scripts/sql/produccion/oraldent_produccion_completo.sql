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

create table if not exists odontogramas (
    id bigserial primary key,
    paciente_id bigint not null,
    usuario_id bigint,
    cita_id bigint,
    observaciones varchar(500),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_odontogramas_paciente foreign key (paciente_id) references pacientes (id),
    constraint fk_odontogramas_usuario foreign key (usuario_id) references usuarios (id),
    constraint fk_odontogramas_cita foreign key (cita_id) references citas (id)
);

create table if not exists odontograma_dientes (
    id bigserial primary key,
    odontograma_id bigint not null,
    numero_fdi integer not null,
    cuadrante integer not null,
    posicion integer not null,
    ausente boolean not null default false,
    implante boolean not null default false,
    corona boolean not null default false,
    endodoncia boolean not null default false,
    extraccion_indicada boolean not null default false,
    movilidad integer,
    observacion varchar(500),
    constraint fk_odontograma_dientes_odontograma foreign key (odontograma_id) references odontogramas (id) on delete cascade,
    constraint ck_odontograma_dientes_cuadrante check (cuadrante between 1 and 4),
    constraint ck_odontograma_dientes_posicion check (posicion between 1 and 8),
    constraint ck_odontograma_dientes_numero_fdi check (numero_fdi between 11 and 48),
    constraint ck_odontograma_dientes_movilidad check (movilidad is null or movilidad between 0 and 3),
    constraint uk_odontograma_dientes_fdi unique (odontograma_id, numero_fdi)
);

create table if not exists odontograma_caras (
    id bigserial primary key,
    diente_id bigint not null,
    tipo varchar(20) not null,
    color varchar(20) not null default 'NINGUNO',
    descripcion varchar(500),
    constraint fk_odontograma_caras_diente foreign key (diente_id) references odontograma_dientes (id) on delete cascade,
    constraint ck_odontograma_caras_tipo check (
        tipo in ('OCLUSAL', 'MESIAL', 'DISTAL', 'VESTIBULAR', 'PALATINO')
    ),
    constraint ck_odontograma_caras_color check (
        color in ('NINGUNO', 'ROJO', 'AZUL')
    ),
    constraint uk_odontograma_caras_tipo unique (diente_id, tipo)
);

create index if not exists idx_pacientes_celular on pacientes (celular);
create index if not exists idx_pacientes_correo on pacientes (correo);
create index if not exists idx_citas_paciente_id on citas (paciente_id);
create index if not exists idx_citas_usuario_id on citas (usuario_id);
create index if not exists idx_citas_servicio_id on citas (servicio_id);
create index if not exists idx_citas_fecha_hora_inicio on citas (fecha_hora_inicio);
create index if not exists idx_horarios_dia_semana on horarios (dia_semana);
create unique index if not exists uk_odontogramas_paciente_activo on odontogramas (paciente_id) where activo;
create index if not exists idx_odontogramas_paciente_id on odontogramas (paciente_id);
create index if not exists idx_odontogramas_usuario_id on odontogramas (usuario_id);
create index if not exists idx_odontogramas_cita_id on odontogramas (cita_id);
create index if not exists idx_odontograma_dientes_odontograma_id on odontograma_dientes (odontograma_id);
create index if not exists idx_odontograma_caras_diente_id on odontograma_caras (diente_id);

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

delete from odontogramas
where paciente_id in (
    select id from pacientes where celular in ('70010001', '70010002', '70010003')
)
or cita_id in (
    select id from citas where codigo_gestion in (
        'ORALA001',
        'ORALA002',
        'ORALA003',
        'ORALA004',
        'ORALA005',
        'ORALA006'
    )
);

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

with semillas(celular, codigo_cita, observaciones) as (
    values
        ('70010001', 'ORALA001', 'Odontograma inicial con hallazgos de valoracion.'),
        ('70010002', 'ORALA002', 'Odontograma inicial para apoyo diagnostico.'),
        ('70010003', 'ORALA003', 'Odontograma inicial de control de ortodoncia.')
)
insert into odontogramas (paciente_id, usuario_id, cita_id, observaciones, activo)
select
    p.id,
    u.id,
    c.id,
    s.observaciones,
    true
from semillas s
join pacientes p on p.celular = s.celular
left join usuarios u on u.correo = 'admin@oraldent.com'
left join citas c on c.codigo_gestion = s.codigo_cita
where not exists (
    select 1
    from odontogramas o
    where o.paciente_id = p.id
      and o.activo = true
);

with posiciones(cuadrante, posicion) as (
    values
        (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
        (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
        (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8),
        (4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8)
)
insert into odontograma_dientes (odontograma_id, numero_fdi, cuadrante, posicion)
select
    o.id,
    posiciones.cuadrante * 10 + posiciones.posicion,
    posiciones.cuadrante,
    posiciones.posicion
from odontogramas o
join pacientes p on p.id = o.paciente_id
cross join posiciones
where p.celular in ('70010001', '70010002', '70010003')
  and not exists (
      select 1
      from odontograma_dientes d
      where d.odontograma_id = o.id
        and d.numero_fdi = posiciones.cuadrante * 10 + posiciones.posicion
  );

with tipos(tipo) as (
    values ('OCLUSAL'), ('MESIAL'), ('DISTAL'), ('VESTIBULAR'), ('PALATINO')
)
insert into odontograma_caras (diente_id, tipo, color)
select d.id, tipos.tipo, 'NINGUNO'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
cross join tipos
where p.celular in ('70010001', '70010002', '70010003')
  and not exists (
      select 1
      from odontograma_caras c
      where c.diente_id = d.id
        and c.tipo = tipos.tipo
  );

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Caries proximal pendiente de tratamiento.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
where c.diente_id = d.id
  and p.celular = '70010001'
  and d.numero_fdi = 16
  and c.tipo = 'MESIAL';

update odontograma_caras c
set color = 'AZUL',
    descripcion = 'Restauracion existente en buen estado.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
where c.diente_id = d.id
  and p.celular = '70010001'
  and d.numero_fdi = 36
  and c.tipo = 'OCLUSAL';

update odontograma_dientes d
set ausente = true,
    observacion = 'Pieza ausente referida por el paciente.'
from odontogramas o
join pacientes p on p.id = o.paciente_id
where d.odontograma_id = o.id
  and p.celular = '70010002'
  and d.numero_fdi = 46;

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Lesion cariosa a evaluar.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
where c.diente_id = d.id
  and p.celular = '70010002'
  and d.numero_fdi = 24
  and c.tipo = 'DISTAL';

update odontograma_caras c
set color = 'AZUL',
    descripcion = 'Sellado/restauracion estetica existente.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
where c.diente_id = d.id
  and p.celular = '70010003'
  and d.numero_fdi = 11
  and c.tipo = 'VESTIBULAR';

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Controlar desgaste incisal.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join pacientes p on p.id = o.paciente_id
where c.diente_id = d.id
  and p.celular = '70010003'
  and d.numero_fdi = 31
  and c.tipo = 'OCLUSAL';

commit;
