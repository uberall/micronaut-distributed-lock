CREATE TABLE IF NOT EXISTS `distributed_lock`
(
    `id`    bigint(20)          NOT NULL AUTO_INCREMENT,
    `name`  varchar(255) UNIQUE NOT NULL,
    `until` datetime            NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
