-- public.alert_config definition

-- Drop table

-- DROP TABLE public.alert_config;

CREATE TABLE public.alert_config (
	id varchar(50) NOT NULL,
	task_id varchar(50) NULL,
	failure_rate_threshold int4 DEFAULT 50 NOT NULL,
	check_interval int4 DEFAULT 30 NOT NULL,
	api_url text NOT NULL,
	http_method varchar(10) DEFAULT 'POST'::character varying NOT NULL,
	headers text NULL,
	body text NULL,
	enabled bool DEFAULT false NULL,
	create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	last_check_time timestamp NULL,
	CONSTRAINT alert_config_pkey PRIMARY KEY (id),
	CONSTRAINT alert_config_task_id_key UNIQUE (task_id)
);
CREATE INDEX idx_alert_config_task_id ON public.alert_config USING btree (task_id);

-- Table Triggers

create trigger update_alert_config_update_time before
update
    on
    public.alert_config for each row execute function update_updated_time_column();


-- public.alert_record definition

-- Drop table

-- DROP TABLE public.alert_record;

CREATE TABLE public.alert_record (
	id varchar(50) NOT NULL,
	task_id varchar(50) NOT NULL,
	task_name varchar(255) NOT NULL,
	failure_rate int4 NOT NULL,
	failure_count int4 NOT NULL,
	total_count int4 NOT NULL,
	alert_message text NULL,
	api_url text NULL,
	response text NULL,
	alert_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT alert_record_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_alert_record_alert_time ON public.alert_record USING btree (alert_time);
CREATE INDEX idx_alert_record_task_id ON public.alert_record USING btree (task_id);


-- public.api_assertion definition

-- Drop table

-- DROP TABLE public.api_assertion;

CREATE TABLE public.api_assertion (
	id varchar(50) NOT NULL,
	task_id varchar(50) NOT NULL,
	response_id varchar(50) NULL,
	assertion_type varchar(20) NOT NULL,
	expected_value text NOT NULL,
	actual_value text NULL,
	passed bool NULL,
	error_message text NULL,
	sort_order int4 DEFAULT 0 NULL,
	create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT api_assertion_pkey PRIMARY KEY (id),
	CONSTRAINT uk_api_assertion_task_id UNIQUE (task_id)
);
CREATE INDEX idx_api_assertion_response_id ON public.api_assertion USING btree (response_id);


-- public.api_response definition

-- Drop table

-- DROP TABLE public.api_response;

CREATE TABLE public.api_response (
	id varchar(50) DEFAULT gen_random_uuid() NOT NULL,
	task_id varchar(50) NOT NULL,
	request_url text NOT NULL,
	request_method varchar(10) NOT NULL,
	request_headers text NULL,
	request_params text NULL,
	response_code int4 NULL,
	response_body text NULL,
	response_time int8 NULL,
	status varchar(20) NOT NULL,
	error_message text NULL,
	execute_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	assertion_result text NULL,
	all_assertions_passed bool NULL,
	CONSTRAINT api_response_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_api_response_execute_time ON public.api_response USING btree (execute_time);
CREATE INDEX idx_api_response_task_id ON public.api_response USING btree (task_id);


-- public.api_task definition

-- Drop table

-- DROP TABLE public.api_task;

CREATE TABLE public.api_task (
	id varchar(50) DEFAULT gen_random_uuid() NOT NULL,
	task_name varchar(255) NOT NULL,
	url text NOT NULL,
	"method" varchar(10) DEFAULT 'GET'::character varying NOT NULL,
	timeout int4 DEFAULT 30000 NULL,
	headers text NULL,
	parameters text NULL,
	cron_expression varchar(100) NOT NULL,
	status varchar(20) DEFAULT 'PAUSED'::character varying NULL,
	description text NULL,
	create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	last_execute_time timestamp NULL,
	assertions text NULL,
	alert_enabled bool DEFAULT false NULL,
	CONSTRAINT api_task_pkey PRIMARY KEY (id)
);

-- Table Triggers

create trigger update_api_task_update_time before
update
    on
    public.api_task for each row execute function update_updated_time_column();


-- public.sys_user definition

-- Drop table

-- DROP TABLE public.sys_user;

CREATE TABLE public.sys_user (
	id varchar(50) NOT NULL,
	username varchar(50) NOT NULL,
	"password" varchar(100) NOT NULL,
	enabled bool DEFAULT true NULL,
	create_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	update_time timestamp DEFAULT CURRENT_TIMESTAMP NULL,
	CONSTRAINT sys_user_pkey PRIMARY KEY (id),
	CONSTRAINT sys_user_username_key UNIQUE (username)
);
CREATE INDEX idx_user_username ON public.sys_user USING btree (username);

-- Table Triggers

create trigger update_user_update_time before
update
    on
    public.sys_user for each row execute function update_updated_time_column();



INSERT INTO public.sys_user
(id, username, "password", enabled, create_time, update_time)
VALUES('admin-001', 'admin', '0192023a7bbd73250516f069df18b500', true, '2025-11-23 23:42:19.946', '2025-11-23 23:42:19.946');