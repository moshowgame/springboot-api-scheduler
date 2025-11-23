-- public.qrtz_calendars definition

-- Drop table

-- DROP TABLE public.qrtz_calendars;

CREATE TABLE public.qrtz_calendars (
                                       sched_name varchar(120) NOT NULL,
                                       calendar_name varchar(200) NOT NULL,
                                       calendar bytea NOT NULL,
                                       CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name)
);


-- public.qrtz_fired_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_fired_triggers;

CREATE TABLE public.qrtz_fired_triggers (
                                            sched_name varchar(120) NOT NULL,
                                            entry_id varchar(95) NOT NULL,
                                            trigger_name varchar(200) NOT NULL,
                                            trigger_group varchar(200) NOT NULL,
                                            instance_name varchar(200) NOT NULL,
                                            fired_time int8 NOT NULL,
                                            sched_time int8 NOT NULL,
                                            priority int4 NOT NULL,
                                            state varchar(16) NOT NULL,
                                            job_name varchar(200) NULL,
                                            job_group varchar(200) NULL,
                                            is_nonconcurrent bool NULL,
                                            requests_recovery bool NULL,
                                            CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id)
);
CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON public.qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);
CREATE INDEX idx_qrtz_ft_j_g ON public.qrtz_fired_triggers USING btree (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_ft_jg ON public.qrtz_fired_triggers USING btree (sched_name, job_group);
CREATE INDEX idx_qrtz_ft_t_g ON public.qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);
CREATE INDEX idx_qrtz_ft_tg ON public.qrtz_fired_triggers USING btree (sched_name, trigger_group);
CREATE INDEX idx_qrtz_ft_trig_inst_name ON public.qrtz_fired_triggers USING btree (sched_name, instance_name);


-- public.qrtz_job_details definition

-- Drop table

-- DROP TABLE public.qrtz_job_details;

CREATE TABLE public.qrtz_job_details (
                                         sched_name varchar(120) NOT NULL,
                                         job_name varchar(200) NOT NULL,
                                         job_group varchar(200) NOT NULL,
                                         description varchar(250) NULL,
                                         job_class_name varchar(250) NOT NULL,
                                         is_durable bool NOT NULL,
                                         is_nonconcurrent bool NOT NULL,
                                         is_update_data bool NOT NULL,
                                         requests_recovery bool NOT NULL,
                                         job_data bytea NULL,
                                         CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group)
);
CREATE INDEX idx_qrtz_j_grp ON public.qrtz_job_details USING btree (sched_name, job_group);
CREATE INDEX idx_qrtz_j_req_recovery ON public.qrtz_job_details USING btree (sched_name, requests_recovery);


-- public.qrtz_locks definition

-- Drop table

-- DROP TABLE public.qrtz_locks;

CREATE TABLE public.qrtz_locks (
                                   sched_name varchar(120) NOT NULL,
                                   lock_name varchar(40) NOT NULL,
                                   CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name)
);


-- public.qrtz_paused_trigger_grps definition

-- Drop table

-- DROP TABLE public.qrtz_paused_trigger_grps;

CREATE TABLE public.qrtz_paused_trigger_grps (
                                                 sched_name varchar(120) NOT NULL,
                                                 trigger_group varchar(200) NOT NULL,
                                                 CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group)
);


-- public.qrtz_scheduler_state definition

-- Drop table

-- DROP TABLE public.qrtz_scheduler_state;

CREATE TABLE public.qrtz_scheduler_state (
                                             sched_name varchar(120) NOT NULL,
                                             instance_name varchar(200) NOT NULL,
                                             last_checkin_time int8 NOT NULL,
                                             checkin_interval int8 NOT NULL,
                                             CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name)
);


-- public.qrtz_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_triggers;

CREATE TABLE public.qrtz_triggers (
                                      sched_name varchar(120) NOT NULL,
                                      trigger_name varchar(200) NOT NULL,
                                      trigger_group varchar(200) NOT NULL,
                                      job_name varchar(200) NOT NULL,
                                      job_group varchar(200) NOT NULL,
                                      description varchar(250) NULL,
                                      next_fire_time int8 NULL,
                                      prev_fire_time int8 NULL,
                                      priority int4 NULL,
                                      trigger_state varchar(16) NOT NULL,
                                      trigger_type varchar(8) NOT NULL,
                                      start_time int8 NOT NULL,
                                      end_time int8 NULL,
                                      calendar_name varchar(200) NULL,
                                      misfire_instr int2 NULL,
                                      job_data bytea NULL,
                                      CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                      CONSTRAINT qrtz_triggers_sched_name_job_name_job_group_fkey FOREIGN KEY (sched_name,job_name,job_group) REFERENCES public.qrtz_job_details(sched_name,job_name,job_group)
);
CREATE INDEX idx_qrtz_t_c ON public.qrtz_triggers USING btree (sched_name, calendar_name);
CREATE INDEX idx_qrtz_t_g ON public.qrtz_triggers USING btree (sched_name, trigger_group);
CREATE INDEX idx_qrtz_t_j ON public.qrtz_triggers USING btree (sched_name, job_name, job_group);
CREATE INDEX idx_qrtz_t_jg ON public.qrtz_triggers USING btree (sched_name, job_group);
CREATE INDEX idx_qrtz_t_n_g_state ON public.qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_n_state ON public.qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_next_fire_time ON public.qrtz_triggers USING btree (sched_name, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st ON public.qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);
CREATE INDEX idx_qrtz_t_nft_st_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);
CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);
CREATE INDEX idx_qrtz_t_state ON public.qrtz_triggers USING btree (sched_name, trigger_state);


-- public.qrtz_blob_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_blob_triggers;

CREATE TABLE public.qrtz_blob_triggers (
                                           sched_name varchar(120) NOT NULL,
                                           trigger_name varchar(200) NOT NULL,
                                           trigger_group varchar(200) NOT NULL,
                                           blob_data bytea NULL,
                                           CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                           CONSTRAINT qrtz_blob_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name,trigger_name,trigger_group) REFERENCES public.qrtz_triggers(sched_name,trigger_name,trigger_group)
);


-- public.qrtz_cron_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_cron_triggers;

CREATE TABLE public.qrtz_cron_triggers (
                                           sched_name varchar(120) NOT NULL,
                                           trigger_name varchar(200) NOT NULL,
                                           trigger_group varchar(200) NOT NULL,
                                           cron_expression varchar(120) NOT NULL,
                                           time_zone_id varchar(80) NULL,
                                           CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                           CONSTRAINT qrtz_cron_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name,trigger_name,trigger_group) REFERENCES public.qrtz_triggers(sched_name,trigger_name,trigger_group)
);


-- public.qrtz_simple_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_simple_triggers;

CREATE TABLE public.qrtz_simple_triggers (
                                             sched_name varchar(120) NOT NULL,
                                             trigger_name varchar(200) NOT NULL,
                                             trigger_group varchar(200) NOT NULL,
                                             repeat_count int8 NOT NULL,
                                             repeat_interval int8 NOT NULL,
                                             times_triggered int8 NOT NULL,
                                             CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                             CONSTRAINT qrtz_simple_triggers_sched_name_trigger_name_trigger_group_fkey FOREIGN KEY (sched_name,trigger_name,trigger_group) REFERENCES public.qrtz_triggers(sched_name,trigger_name,trigger_group)
);


-- public.qrtz_simprop_triggers definition

-- Drop table

-- DROP TABLE public.qrtz_simprop_triggers;

CREATE TABLE public.qrtz_simprop_triggers (
                                              sched_name varchar(120) NOT NULL,
                                              trigger_name varchar(200) NOT NULL,
                                              trigger_group varchar(200) NOT NULL,
                                              str_prop_1 varchar(512) NULL,
                                              str_prop_2 varchar(512) NULL,
                                              str_prop_3 varchar(512) NULL,
                                              int_prop_1 int4 NULL,
                                              int_prop_2 int4 NULL,
                                              long_prop_1 int8 NULL,
                                              long_prop_2 int8 NULL,
                                              dec_prop_1 numeric(13, 4) NULL,
                                              dec_prop_2 numeric(13, 4) NULL,
                                              bool_prop_1 bool NULL,
                                              bool_prop_2 bool NULL,
                                              CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group),
                                              CONSTRAINT qrtz_simprop_triggers_sched_name_trigger_name_trigger_grou_fkey FOREIGN KEY (sched_name,trigger_name,trigger_group) REFERENCES public.qrtz_triggers(sched_name,trigger_name,trigger_group)
);



-- ----------------------------
-- Table structure for sys_token
-- ----------------------------
DROP TABLE IF EXISTS sys_token;
CREATE TABLE sys_token  (
                            token_id serial NOT NULL,
                            token_value varchar(255) NULL DEFAULT NULL,
                            PRIMARY KEY (token_id)
);

-- ----------------------------
-- Table structure for url_request
-- ----------------------------
DROP TABLE IF EXISTS url_request;
CREATE TABLE url_request  (
                              request_id varchar(255) NOT NULL,
                              request_name varchar(255) NULL DEFAULT NULL,
                              request_method varchar(255) NULL DEFAULT NULL,
                              request_cron varchar(255) NULL DEFAULT NULL,
                              request_url varchar(255) NULL DEFAULT NULL,
                              status smallint NULL DEFAULT 0,
                              request_timeout int NULL DEFAULT NULL,
                              update_time timestamp NULL DEFAULT NULL,
                              PRIMARY KEY (request_id)
);

-- ----------------------------
-- Table structure for url_request_token
-- ----------------------------
DROP TABLE IF EXISTS url_request_token;
CREATE TABLE url_request_token  (
                                    request_id varchar(50) NOT NULL,
                                    token_url varchar(255) NULL DEFAULT NULL,
                                    method varchar(255) NULL DEFAULT 'POST',
                                    param varchar(255) NULL DEFAULT NULL,
                                    param_type varchar(50) NULL DEFAULT '1',
                                    append_name varchar(255) NULL DEFAULT NULL,
                                    append_type varchar(50) NULL DEFAULT '1',
                                    status smallint NULL DEFAULT 1,
                                    token_expression varchar(255) NULL DEFAULT NULL,
                                    PRIMARY KEY (request_id)
);

-- ----------------------------
-- Table structure for url_response
-- ----------------------------
DROP TABLE IF EXISTS url_response;
CREATE TABLE url_response  (
                               response_id varchar(50) NOT NULL,
                               request_id varchar(50) NULL DEFAULT NULL,
                               response_time timestamp(3) NULL DEFAULT NULL,
                               response_text text NULL DEFAULT NULL,
                               status smallint NULL DEFAULT NULL,
                               request_time timestamp(3) NULL DEFAULT NULL,
                               assumption_result varchar(20) NULL DEFAULT NULL,
                               PRIMARY KEY (response_id)
);

-- ----------------------------
-- Table structure for url_response_assumption
-- ----------------------------
DROP TABLE IF EXISTS url_response_assumption;
CREATE TABLE url_response_assumption  (
                                          request_id varchar(50) NOT NULL,
                                          key_first varchar(100) NULL DEFAULT NULL,
                                          value_first varchar(100) NULL DEFAULT NULL,
                                          key_second varchar(100) NULL DEFAULT NULL,
                                          value_second varchar(100) NULL DEFAULT NULL,
                                          key_third varchar(100) NULL DEFAULT NULL,
                                          value_third varchar(100) NULL DEFAULT NULL,
                                          value_else varchar(100) NULL DEFAULT NULL,
                                          status int NULL DEFAULT 1,
                                          PRIMARY KEY (request_id)
);
