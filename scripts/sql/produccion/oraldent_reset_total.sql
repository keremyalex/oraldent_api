begin;

-- Script destructivo para desarrollo/demo.
-- Elimina tablas existentes, las recrea y vuelve a cargar los datos base.

drop table if exists periodontograma_sitios cascade;
drop table if exists periodontograma_dientes cascade;
drop table if exists periodontogramas cascade;
drop table if exists odontograma_caras cascade;
drop table if exists odontograma_dientes cascade;
drop table if exists odontogramas cascade;
drop table if exists analisis_radiografias cascade;
drop table if exists radiografias cascade;
drop table if exists receta_detalles cascade;
drop table if exists recetas cascade;
drop table if exists fichas_clinicas cascade;
drop table if exists citas cascade;
drop table if exists usuarios cascade;
drop table if exists horarios cascade;
drop table if exists servicios cascade;
drop table if exists pacientes cascade;
drop table if exists anamnesis cascade;

create table pacientes (
    id bigserial primary key,
    nombre varchar(80) not null,
    apellido_paterno varchar(80) not null,
    apellido_materno varchar(80),
    celular varchar(30) not null,
    documento_identidad varchar(30) not null,
    correo varchar(120),
    fecha_nacimiento date,
    direccion varchar(200),
    codigo_paciente varchar(20),
    foto_url varchar(500),
    foto_public_id varchar(255),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint uk_pacientes_codigo_paciente unique (codigo_paciente),
    constraint uk_pacientes_documento_identidad unique (documento_identidad)
);

create table servicios (
    id bigserial primary key,
    nombre varchar(150) not null,
    descripcion varchar(500),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint uk_servicios_nombre unique (nombre)
);

create table horarios (
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

create table usuarios (
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

create table citas (
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

create table fichas_clinicas (
    id bigserial primary key,
    paciente_id bigint not null,
    usuario_id bigint,
    cita_id bigint,
    fecha timestamp without time zone not null default now(),
    edad integer,
    sexo varchar(30),
    procedencia varchar(120),
    ocupacion varchar(120),
    presion_arterial varchar(30),
    temperatura numeric(4, 1),
    pulso integer,
    motivo_consulta varchar(500),
    enfermedad_actual varchar(1000),
    examen_clinico varchar(1200),
    examen_radiografico varchar(1200),
    diagnostico varchar(1200),
    tratamiento varchar(1200),
    tecnica_anestesia varchar(800),
    evolucion varchar(1200),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_fichas_paciente foreign key (paciente_id) references pacientes (id),
    constraint fk_fichas_usuario foreign key (usuario_id) references usuarios (id),
    constraint fk_fichas_cita foreign key (cita_id) references citas (id),
    constraint uk_fichas_cita unique (cita_id),
    constraint ck_fichas_edad check (edad is null or edad between 0 and 130),
    constraint ck_fichas_temperatura check (temperatura is null or temperatura between 30.0 and 45.0),
    constraint ck_fichas_pulso check (pulso is null or pulso between 0 and 260)
);


create table radiografias (
    id bigserial primary key,
    ficha_clinica_id bigint not null,
    titulo varchar(160) not null,
    descripcion varchar(500),
    tipo varchar(80),
    numero_fdi integer,
    zona varchar(80),
    imagen_url varchar(500) not null,
    imagen_public_id varchar(255) not null,
    nombre_archivo varchar(255),
    formato varchar(80),
    tamano_bytes bigint,
    ancho_px integer,
    alto_px integer,
    fecha_estudio date,
    diagnostico_radiografico varchar(1000),
    perdida_osea_observada boolean default false,
    tipo_perdida_osea varchar(30),
    severidad_perdida_osea varchar(30),
    porcentaje_perdida_osea_estimado numeric(5, 2),
    nivel_cresta_osea_mm numeric(6, 2),
    observaciones_periodontales varchar(1000),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_radiografias_ficha foreign key (ficha_clinica_id) references fichas_clinicas (id),
    constraint ck_radiografias_numero_fdi check (numero_fdi is null or numero_fdi between 11 and 48),
    constraint ck_radiografias_porcentaje_perdida check (porcentaje_perdida_osea_estimado is null or porcentaje_perdida_osea_estimado between 0 and 100),
    constraint ck_radiografias_nivel_cresta check (nivel_cresta_osea_mm is null or nivel_cresta_osea_mm >= 0)
);

create table analisis_radiografias (
    id bigserial primary key,
    radiografia_id bigint not null,
    modelo varchar(120),
    estado varchar(30) not null default 'PENDIENTE',
    perdida_osea_detectada boolean default false,
    tipo_perdida_osea varchar(30),
    severidad varchar(30),
    porcentaje_perdida_osea numeric(5, 2),
    confianza numeric(5, 4),
    resultado_json text,
    recomendacion varchar(1000),
    error_analisis varchar(1000),
    validado boolean default false,
    validado_por_usuario_id bigint,
    fecha_validacion timestamp without time zone,
    comentario_validacion varchar(1000),
    severidad_final varchar(30),
    tipo_perdida_osea_final varchar(30),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_analisis_radiografia foreign key (radiografia_id) references radiografias (id) on delete cascade,
    constraint fk_analisis_radiografia_usuario foreign key (validado_por_usuario_id) references usuarios (id),
    constraint ck_analisis_porcentaje_perdida check (porcentaje_perdida_osea is null or porcentaje_perdida_osea between 0 and 100),
    constraint ck_analisis_confianza check (confianza is null or confianza between 0 and 1)
);
create table anamnesis (
    id bigserial primary key,
    ficha_clinica_id bigint not null unique,
    descripcion varchar(1000),
    hemorragia boolean default false,
    diabetes boolean default false,
    hipertension boolean default false,
    epilepsia boolean default false,
    problemas_cardiovasculares boolean default false,
    lipotimias boolean default false,
    tratamiento_medico_actual boolean default false,
    alergias varchar(500),
    medicamento_actual varchar(500),
    otras_patologias varchar(500),
    constraint fk_anamnesis_ficha foreign key (ficha_clinica_id) references fichas_clinicas (id) on delete cascade
);

create table odontogramas (
    id bigserial primary key,
    ficha_clinica_id bigint not null,
    observaciones varchar(500),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_odontogramas_ficha foreign key (ficha_clinica_id) references fichas_clinicas (id)
);

create table odontograma_dientes (
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
    observacion varchar(500),
    constraint fk_odontograma_dientes_odontograma foreign key (odontograma_id) references odontogramas (id) on delete cascade,
    constraint ck_odontograma_dientes_cuadrante check (cuadrante between 1 and 4),
    constraint ck_odontograma_dientes_posicion check (posicion between 1 and 8),
    constraint ck_odontograma_dientes_numero_fdi check (numero_fdi between 11 and 48),
    constraint uk_odontograma_dientes_fdi unique (odontograma_id, numero_fdi)
);

create table odontograma_caras (
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

create table periodontogramas (
    id bigserial primary key,
    ficha_clinica_id bigint not null,
    observaciones varchar(500),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_periodontogramas_ficha foreign key (ficha_clinica_id) references fichas_clinicas (id)
);

create table periodontograma_dientes (
    id bigserial primary key,
    periodontograma_id bigint not null,
    numero_fdi integer not null,
    cuadrante integer not null,
    posicion integer not null,
    ausente boolean not null default false,
    implante boolean not null default false,
    movilidad integer,
    furcacion_vestibular varchar(20) not null default 'NINGUNA',
    furcacion_palatina_lingual varchar(20) not null default 'NINGUNA',
    observacion varchar(500),
    constraint fk_periodontograma_dientes_periodontograma foreign key (periodontograma_id) references periodontogramas (id) on delete cascade,
    constraint ck_periodontograma_dientes_cuadrante check (cuadrante between 1 and 4),
    constraint ck_periodontograma_dientes_posicion check (posicion between 1 and 8),
    constraint ck_periodontograma_dientes_numero_fdi check (numero_fdi between 11 and 48),
    constraint ck_periodontograma_dientes_movilidad check (movilidad is null or movilidad between 0 and 3),
    constraint ck_periodontograma_dientes_furcacion_vestibular check (furcacion_vestibular in ('NINGUNA', 'GRADO_I', 'GRADO_II', 'GRADO_III')),
    constraint ck_periodontograma_dientes_furcacion_palatina_lingual check (furcacion_palatina_lingual in ('NINGUNA', 'GRADO_I', 'GRADO_II', 'GRADO_III')),
    constraint uk_periodontograma_dientes_fdi unique (periodontograma_id, numero_fdi)
);

create table periodontograma_sitios (
    id bigserial primary key,
    diente_id bigint not null,
    sitio varchar(30) not null,
    sangrado_sondaje boolean not null default false,
    placa boolean not null default false,
    supuracion boolean not null default false,
    margen_gingival_mm integer not null default 0,
    profundidad_sondaje_mm integer not null default 0,
    observacion varchar(500),
    constraint fk_periodontograma_sitios_diente foreign key (diente_id) references periodontograma_dientes (id) on delete cascade,
    constraint ck_periodontograma_sitios_tipo check (
        sitio in ('MESIOVESTIBULAR', 'VESTIBULAR', 'DISTOVESTIBULAR', 'MESIOPALATINO', 'PALATINO', 'DISTOPALATINO')
    ),
    constraint ck_periodontograma_sitios_margen check (margen_gingival_mm between -20 and 20),
    constraint ck_periodontograma_sitios_profundidad check (profundidad_sondaje_mm between 0 and 20),
    constraint uk_periodontograma_sitios_tipo unique (diente_id, sitio)
);

create table recetas (
    id bigserial primary key,
    ficha_clinica_id bigint not null,
    usuario_id bigint,
    indicaciones_generales varchar(1200),
    observaciones varchar(1200),
    activo boolean not null default true,
    fecha_creacion timestamp without time zone not null default now(),
    fecha_actualizacion timestamp without time zone not null default now(),
    constraint fk_recetas_ficha foreign key (ficha_clinica_id) references fichas_clinicas (id),
    constraint fk_recetas_usuario foreign key (usuario_id) references usuarios (id)
);

create table receta_detalles (
    id bigserial primary key,
    receta_id bigint not null,
    medicamento varchar(160) not null,
    dosis varchar(120),
    frecuencia varchar(120),
    duracion varchar(120),
    indicaciones varchar(500),
    orden integer not null default 0,
    constraint fk_receta_detalles_receta foreign key (receta_id) references recetas (id) on delete cascade,
    constraint ck_receta_detalles_orden check (orden >= 0)
);

create index idx_pacientes_celular on pacientes (celular);
create index idx_pacientes_correo on pacientes (correo);
create index idx_pacientes_documento_identidad on pacientes (documento_identidad);
create index idx_citas_paciente_id on citas (paciente_id);
create index idx_citas_usuario_id on citas (usuario_id);
create index idx_citas_servicio_id on citas (servicio_id);
create index idx_citas_fecha_hora_inicio on citas (fecha_hora_inicio);
create index idx_horarios_dia_semana on horarios (dia_semana);
create index idx_fichas_paciente_id on fichas_clinicas (paciente_id);
create index idx_fichas_cita_id on fichas_clinicas (cita_id);
create index idx_odontogramas_ficha_id on odontogramas (ficha_clinica_id);
create index idx_odontograma_dientes_odontograma_id on odontograma_dientes (odontograma_id);
create index idx_odontograma_caras_diente_id on odontograma_caras (diente_id);
create index idx_periodontogramas_ficha_id on periodontogramas (ficha_clinica_id);
create index idx_periodontograma_dientes_periodontograma_id on periodontograma_dientes (periodontograma_id);
create index idx_periodontograma_sitios_diente_id on periodontograma_sitios (diente_id);
create index idx_recetas_ficha_id on recetas (ficha_clinica_id);
create index idx_recetas_usuario_id on recetas (usuario_id);
create index idx_receta_detalles_receta_id on receta_detalles (receta_id);
create index idx_radiografias_ficha_id on radiografias (ficha_clinica_id);

insert into servicios (nombre, descripcion, activo)
values
    ('Implantologia Dental', 'Tratamientos relacionados con implantes dentales.', true),
    ('Rayo X', 'Toma y apoyo diagnostico radiografico.', true),
    ('Blanqueamiento Dental Laser', 'Tratamiento estetico para aclaramiento dental.', true),
    ('Ortodoncia', 'Correccion de alineacion y mordida.', true),
    ('Protesis Fija y Removible', 'Rehabilitacion oral con protesis parciales o completas.', true);

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
);

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

update pacientes
set codigo_paciente = 'PAC-' || lpad(id::text, 6, '0')
where codigo_paciente is null;

alter table pacientes alter column codigo_paciente set not null;

insert into fichas_clinicas (
    paciente_id,
    usuario_id,
    cita_id,
    fecha,
    edad,
    sexo,
    procedencia,
    ocupacion,
    presion_arterial,
    temperatura,
    pulso,
    motivo_consulta,
    enfermedad_actual,
    examen_clinico,
    examen_radiografico,
    diagnostico,
    tratamiento,
    tecnica_anestesia,
    evolucion,
    activo
)
values
    (
        (select id from pacientes where celular = '70010001' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from citas where codigo_gestion = 'ORALA001' limit 1),
        '2026-04-27 09:00:00',
        29,
        'No especificado',
        'Cochabamba',
        'Comerciante',
        '120/80',
        36.7,
        78,
        'Valoracion inicial para implante',
        'Paciente refiere ausencia de pieza posterior y desea evaluar rehabilitacion con implante.',
        'Ausencia de pieza posterior y mucosa sin lesiones aparentes.',
        'Se solicita radiografia panoramica para evaluar disponibilidad osea.',
        'Edentulismo parcial con indicacion de evaluacion para implante.',
        'Planificar fase diagnostica para rehabilitacion implantosoportada.',
        'No aplicada en esta consulta.',
        'Pendiente de control segun plan de tratamiento.',
        true
    ),
    (
        (select id from pacientes where celular = '70010002' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from citas where codigo_gestion = 'ORALA002' limit 1),
        '2026-04-27 15:30:00',
        33,
        'No especificado',
        'Cochabamba',
        'Estudiante',
        '115/75',
        36.7,
        74,
        'Radiografia panoramica de control',
        'Control por molestia al masticar y revision de pieza ausente.',
        'Encías con inflamacion leve localizada y control de placa indicado.',
        'Radiografia de control sin lesiones periapicales evidentes.',
        'Control odontologico con hallazgos leves.',
        'Profilaxis, control de higiene y seguimiento.',
        'No aplicada en esta consulta.',
        'Pendiente de control segun plan de tratamiento.',
        true
    ),
    (
        (select id from pacientes where celular = '70010003' limit 1),
        (select id from usuarios where correo = 'admin@oraldent.com' limit 1),
        (select id from citas where codigo_gestion = 'ORALA003' limit 1),
        '2026-04-28 10:00:00',
        25,
        'No especificado',
        'Cochabamba',
        'Estudiante',
        '115/75',
        36.7,
        74,
        'Control de ortodoncia',
        'Ajuste mensual y evaluacion de higiene durante tratamiento ortodontico.',
        'Encías con inflamacion leve localizada y control de placa indicado.',
        'Radiografia de control sin lesiones periapicales evidentes.',
        'Control odontologico con hallazgos leves.',
        'Profilaxis, control de higiene y seguimiento.',
        'No aplicada en esta consulta.',
        'Pendiente de control segun plan de tratamiento.',
        true
    );

insert into anamnesis (
    ficha_clinica_id,
    descripcion,
    hemorragia,
    diabetes,
    hipertension,
    epilepsia,
    problemas_cardiovasculares,
    lipotimias,
    tratamiento_medico_actual,
    alergias,
    medicamento_actual,
    otras_patologias
)
select
    f.id,
    s.descripcion,
    s.hemorragia,
    s.diabetes,
    s.hipertension,
    s.epilepsia,
    s.problemas_cardiovasculares,
    s.lipotimias,
    s.tratamiento_medico_actual,
    s.alergias,
    s.medicamento_actual,
    s.otras_patologias
from (
    values
        (
            'ORALA001',
            'Sin antecedentes sistemicos relevantes. Refiere sensibilidad ocasional en sector posterior.',
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            'Niega alergias medicamentosas.',
            null,
            'Sin otras patologias referidas.'
        ),
        (
            'ORALA002',
            'Paciente refiere tratamiento medico anterior por hipertension controlada.',
            false,
            false,
            true,
            false,
            false,
            false,
            true,
            null,
            'Losartan 50 mg indicado por medico tratante.',
            'Sin otras patologias referidas.'
        ),
        (
            'ORALA003',
            'Paciente sin alergias conocidas. Buen estado general.',
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            null,
            null,
            'Sin otras patologias referidas.'
        )
) as s(
    codigo_cita,
    descripcion,
    hemorragia,
    diabetes,
    hipertension,
    epilepsia,
    problemas_cardiovasculares,
    lipotimias,
    tratamiento_medico_actual,
    alergias,
    medicamento_actual,
    otras_patologias
)
join citas c on c.codigo_gestion = s.codigo_cita
join fichas_clinicas f on f.cita_id = c.id;

with semillas(celular, codigo_cita, observaciones) as (
    values
        ('70010001', 'ORALA001', 'Odontograma inicial con hallazgos de valoracion.'),
        ('70010002', 'ORALA002', 'Odontograma inicial para apoyo diagnostico.'),
        ('70010003', 'ORALA003', 'Odontograma inicial de control de ortodoncia.')
)
insert into odontogramas (ficha_clinica_id, observaciones, activo)
select
    f.id,
    s.observaciones,
    true
from semillas s
join citas c on c.codigo_gestion = s.codigo_cita
join fichas_clinicas f on f.cita_id = c.id;

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
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
cross join posiciones
where p.celular in ('70010001', '70010002', '70010003');

with tipos(tipo) as (
    values ('OCLUSAL'), ('MESIAL'), ('DISTAL'), ('VESTIBULAR'), ('PALATINO')
)
insert into odontograma_caras (diente_id, tipo, color)
select d.id, tipos.tipo, 'NINGUNO'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
cross join tipos
where p.celular in ('70010001', '70010002', '70010003');

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Caries proximal pendiente de tratamiento.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where c.diente_id = d.id
  and p.celular = '70010001'
  and d.numero_fdi = 16
  and c.tipo = 'MESIAL';

update odontograma_caras c
set color = 'AZUL',
    descripcion = 'Restauracion existente en buen estado.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where c.diente_id = d.id
  and p.celular = '70010001'
  and d.numero_fdi = 36
  and c.tipo = 'OCLUSAL';

update odontograma_dientes d
set ausente = true,
    observacion = 'Pieza ausente referida por el paciente.'
from odontogramas o
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where d.odontograma_id = o.id
  and p.celular = '70010002'
  and d.numero_fdi = 46;

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Lesion cariosa a evaluar.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where c.diente_id = d.id
  and p.celular = '70010002'
  and d.numero_fdi = 24
  and c.tipo = 'DISTAL';

update odontograma_caras c
set color = 'AZUL',
    descripcion = 'Sellado/restauracion estetica existente.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where c.diente_id = d.id
  and p.celular = '70010003'
  and d.numero_fdi = 11
  and c.tipo = 'VESTIBULAR';

update odontograma_caras c
set color = 'ROJO',
    descripcion = 'Controlar desgaste incisal.'
from odontograma_dientes d
join odontogramas o on o.id = d.odontograma_id
join fichas_clinicas f on f.id = o.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
where c.diente_id = d.id
  and p.celular = '70010003'
  and d.numero_fdi = 31
  and c.tipo = 'OCLUSAL';

with semillas(celular, codigo_cita, observaciones) as (
    values
        ('70010001', 'ORALA001', 'Periodontograma inicial con control de sondaje.'),
        ('70010002', 'ORALA002', 'Periodontograma de valoracion periodontal inicial.'),
        ('70010003', 'ORALA003', 'Periodontograma de seguimiento de control.')
)
insert into periodontogramas (ficha_clinica_id, observaciones, activo)
select
    f.id,
    s.observaciones,
    true
from semillas s
join citas c on c.codigo_gestion = s.codigo_cita
join fichas_clinicas f on f.cita_id = c.id;

with posiciones(cuadrante, posicion) as (
    values
        (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
        (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8),
        (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8),
        (4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 6), (4, 7), (4, 8)
)
insert into periodontograma_dientes (periodontograma_id, numero_fdi, cuadrante, posicion)
select
    p.id,
    posiciones.cuadrante * 10 + posiciones.posicion,
    posiciones.cuadrante,
    posiciones.posicion
from periodontogramas p
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
cross join posiciones
where pa.celular in ('70010001', '70010002', '70010003');

with sitios(sitio) as (
    values
        ('MESIOVESTIBULAR'),
        ('VESTIBULAR'),
        ('DISTOVESTIBULAR'),
        ('MESIOPALATINO'),
        ('PALATINO'),
        ('DISTOPALATINO')
)
insert into periodontograma_sitios (diente_id, sitio)
select d.id, sitios.sitio
from periodontograma_dientes d
join periodontogramas p on p.id = d.periodontograma_id
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
cross join sitios
where pa.celular in ('70010001', '70010002', '70010003');

update periodontograma_dientes d
set movilidad = 1,
    furcacion_vestibular = 'GRADO_I',
    observacion = 'Molestia localizada en control periodontal.'
from periodontogramas p
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
where d.periodontograma_id = p.id
  and pa.celular = '70010001'
  and d.numero_fdi = 16;

update periodontograma_dientes
set furcacion_vestibular = 'NINGUNA',
    furcacion_palatina_lingual = 'NINGUNA'
where numero_fdi not in (16, 17, 18, 26, 27, 28, 36, 37, 38, 46, 47, 48);

update periodontograma_dientes d
set implante = true,
    observacion = 'Implante en seguimiento periodontal.'
from periodontogramas p
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
where d.periodontograma_id = p.id
  and pa.celular = '70010002'
  and d.numero_fdi = 46;

update periodontograma_sitios s
set sangrado_sondaje = true,
    placa = true,
    margen_gingival_mm = 1,
    profundidad_sondaje_mm = 5,
    observacion = 'Sangrado positivo y bolsa periodontal localizada.'
from periodontograma_dientes d
join periodontogramas p on p.id = d.periodontograma_id
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
where s.diente_id = d.id
  and pa.celular = '70010001'
  and d.numero_fdi = 16
  and s.sitio = 'MESIOVESTIBULAR';

update periodontograma_sitios s
set supuracion = true,
    margen_gingival_mm = 2,
    profundidad_sondaje_mm = 6,
    observacion = 'Supuracion al sondaje en control.'
from periodontograma_dientes d
join periodontogramas p on p.id = d.periodontograma_id
join fichas_clinicas f on f.id = p.ficha_clinica_id
join pacientes pa on pa.id = f.paciente_id
where s.diente_id = d.id
  and pa.celular = '70010003'
  and d.numero_fdi = 36
  and s.sitio = 'DISTOPALATINO';

insert into recetas (ficha_clinica_id, usuario_id, indicaciones_generales, observaciones, activo)
select f.id, 1,
       'Tomar los medicamentos segun indicacion profesional y completar el tratamiento.',
       'Receta inicial de control clinico.',
       true
from fichas_clinicas f
join pacientes p on p.id = f.paciente_id
where p.celular = '70010001'
  and not exists (
      select 1 from recetas r where r.ficha_clinica_id = f.id
  );

insert into recetas (ficha_clinica_id, usuario_id, indicaciones_generales, observaciones, activo)
select f.id, 1,
       'Mantener higiene oral estricta y seguir esquema antibiotico si fue indicado.',
       'Receta para manejo de dolor y control.',
       true
from fichas_clinicas f
join pacientes p on p.id = f.paciente_id
where p.celular = '70010002'
  and not exists (
      select 1 from recetas r where r.ficha_clinica_id = f.id
  );

insert into receta_detalles (receta_id, medicamento, dosis, frecuencia, duracion, indicaciones, orden)
select r.id, d.medicamento, d.dosis, d.frecuencia, d.duracion, d.indicaciones, d.orden
from recetas r
join fichas_clinicas f on f.id = r.ficha_clinica_id
join pacientes p on p.id = f.paciente_id
join (
    values
        ('70010001', 'Ibuprofeno', '400 mg', 'Cada 8 horas', '5 dias', 'Tomar despues de las comidas.', 0),
        ('70010001', 'Clorhexidina', '15 ml', 'Cada 12 horas', '7 dias', 'Enjuague bucal sin ingerir.', 1),
        ('70010002', 'Paracetamol', '500 mg', 'Cada 8 horas si hay dolor', '3 dias', 'No exceder la dosis diaria.', 0),
        ('70010002', 'Amoxicilina', '500 mg', 'Cada 8 horas', '7 dias', 'Completar el tratamiento salvo contraindicacion.', 1)
) as d(celular, medicamento, dosis, frecuencia, duracion, indicaciones, orden)
    on d.celular = p.celular
where not exists (
    select 1
    from receta_detalles rd
    where rd.receta_id = r.id
      and rd.orden = d.orden
);


insert into radiografias (ficha_clinica_id, titulo, descripcion, tipo, numero_fdi, zona, imagen_url, imagen_public_id, fecha_estudio, diagnostico_radiografico, perdida_osea_observada, tipo_perdida_osea, severidad_perdida_osea, porcentaje_perdida_osea_estimado, observaciones_periodontales, activo)
select f.id,
       'Radiografia panoramica inicial',
       'Imagen de apoyo diagnostico para la ficha clinica.',
       'PANORAMICA',
       null,
       'GENERAL',
       'https://res.cloudinary.com/demo/image/upload/sample.jpg',
       'oraldent/demo/radiografia-panoramica-inicial',
       f.fecha::date,
       'Evaluacion panoramica de apoyo diagnostico.',
       false,
       'NO_EVALUABLE',
       'NO_EVALUABLE',
       null,
       'Imagen referencial para historial clinico.',
       true
from fichas_clinicas f
join pacientes p on p.id = f.paciente_id
where p.celular = '70010001'
  and not exists (
      select 1 from radiografias r where r.ficha_clinica_id = f.id and r.titulo = 'Radiografia panoramica inicial'
  );

insert into radiografias (ficha_clinica_id, titulo, descripcion, tipo, numero_fdi, zona, imagen_url, imagen_public_id, fecha_estudio, diagnostico_radiografico, perdida_osea_observada, tipo_perdida_osea, severidad_perdida_osea, porcentaje_perdida_osea_estimado, observaciones_periodontales, activo)
select f.id,
       'Radiografia periapical de control',
       'Control radiografico localizado para seguimiento.',
       'PERIAPICAL',
       36,
       'POSTERIOR_INFERIOR',
       'https://res.cloudinary.com/demo/image/upload/sample.jpg',
       'oraldent/demo/radiografia-periapical-control',
       f.fecha::date,
       'Cresta osea con perdida horizontal compatible con compromiso periodontal.',
       true,
       'HORIZONTAL',
       'MODERADA',
       32.50,
       'Se sugiere correlacionar con sondaje periodontal.',
       true
from fichas_clinicas f
join pacientes p on p.id = f.paciente_id
where p.celular = '70010002'
  and not exists (
      select 1 from radiografias r where r.ficha_clinica_id = f.id and r.titulo = 'Radiografia periapical de control'
  );
insert into analisis_radiografias (
    radiografia_id,
    modelo,
    estado,
    perdida_osea_detectada,
    tipo_perdida_osea,
    severidad,
    porcentaje_perdida_osea,
    confianza,
    resultado_json,
    recomendacion,
    validado,
    severidad_final,
    tipo_perdida_osea_final,
    activo
)
select
    r.id,
    'perdida-osea-demo-v1',
    'COMPLETADO',
    true,
    'HORIZONTAL',
    'MODERADA',
    32.50,
    0.8700,
    '{"hallazgo":"perdida osea horizontal estimada","pieza":36}',
    'Revisar evolucion periodontal y comparar con periodontograma.',
    false,
    null,
    null,
    true
from radiografias r
where r.tipo = 'PERIAPICAL'
  and r.numero_fdi = 36
  and not exists (
      select 1 from analisis_radiografias ar where ar.radiografia_id = r.id
  );

commit;



