# osbot_accountmanager
an osbot.org account manager made for Runescape botting (still in development)

Preview
![alt text](https://i.gyazo.com/2f3ba3927751d968c722b62bca69339a.png)

this includes all the following:

- creating and recovering accounts via web automation libraries for Java (selenium)
  - account creation
    - this means: filling in a form, solving a captcha (2captcha, via a captcha solver) and submitting a post request
    - going to the email, checking for the correct e-mail address
    - clicking on a link
  - account recovery
    - when set to: locked, the account has to be recovered, doing:
      - filling in another form and sending post request
      - got e-mail, clicking on recover link
      - setting new password in form and submitting form
- doing several tasks
  - doing tutorial island
  - a 7qp script, including:
    - dorics quest
    - cooks assistant
    - romeo & juliet
    - sheep shearer
  - automatic muling between multiple servers
    - using normal mules, super mules and server mules
  - automatic tasks for g.e. selling and buying items
  
all the money transfers to one account, for me to manually trade over all the money from over 500 accounts at one time
  
# sql
```sql
-- --------------------------------------------------------
-- Host:                         173.208.242.210
-- Server versie:                10.3.16-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Versie:              9.5.0.5196
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Databasestructuur van 142.54.191.98 wordt geschreven
CREATE DATABASE IF NOT EXISTS `142.54.191.98` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `142.54.191.98`;

-- Structuur van  tabel 142.54.191.98.account wordt geschreven
CREATE TABLE IF NOT EXISTS `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `password` varchar(50) DEFAULT NULL,
  `bank_pin` varchar(50) DEFAULT NULL,
  `visible` varchar(50) DEFAULT 'true',
  `trade_with_other` varchar(50) DEFAULT NULL,
  `country_code_proxy_usage` varchar(50) DEFAULT NULL,
  `account_stage` enum('TUT_ISLAND','QUEST_COOK_ASSISTANT','QUEST_ROMEO_AND_JULIET','UNKNOWN','QUEST_SHEEP_SHEARER','MINING_LEVEL_TO_15','MINING_IRON_ORE','MULE_TRADING','RIMMINGTON_IRON_ORE','GE_SELL_BUY_MINING','QUEST_DORICS_QUEST','WOODCUTTING_GOLD_FARM','MINING_RIMMINGTON_CLAY') DEFAULT 'TUT_ISLAND',
  `account_stage_progress` int(11) DEFAULT 0,
  `login_status` enum('INITIALIZING','LOGGED_IN','DEFAULT') DEFAULT 'DEFAULT',
  `status` enum('BANNED','AVAILABLE','LOCKED','MANUAL_REVIEW','NOT_VERIFIED','TIMEOUT','LOCKED_INGAME','WALKING_STUCK','TASK_TIMEOUT','OUT_OF_MONEY','MULE','LOCKED_TIMEOUT','INVALID_PASSWORD','SUPER_MULE','SERVER_MULE') DEFAULT 'AVAILABLE',
  `account_value` int(11) DEFAULT 0,
  `quest_points` int(11) DEFAULT 0,
  `day` int(11) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `email` varchar(200) DEFAULT NULL,
  `proxy_ip` varchar(50) DEFAULT NULL,
  `proxy_port` varchar(50) DEFAULT NULL,
  `world_number` int(11) DEFAULT NULL,
  `low_cpu_mode` tinytext DEFAULT NULL,
  `amount_timeout` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL ON UPDATE current_timestamp(),
  `break_till` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26079 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel 142.54.191.98.config wordt geschreven
CREATE TABLE IF NOT EXISTS `config` (
  `script` enum('CLAY_ORE','OAK_LOGS','IRON_MINING','RIMMINGTON_MINING','EAST_OF_VARROCK_IRON_MINING') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel 142.54.191.98.proxies wordt geschreven
CREATE TABLE IF NOT EXISTS `proxies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_addres` varchar(50) NOT NULL,
  `port` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `mule_proxy` tinyint(4) NOT NULL DEFAULT 0,
  `is_alive` tinyint(4) NOT NULL DEFAULT 0,
  `error_ip` tinyint(4) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=509 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd

-- Databasestructuur van logging wordt geschreven
CREATE DATABASE IF NOT EXISTS `logging` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `logging`;

-- Structuur van  tabel logging.log wordt geschreven
CREATE TABLE IF NOT EXISTS `log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` enum('DEFAULT','GOLD_TRANSFER','ERROR','DEPOSIT_BANK_ITEM','CAPTCHA','WEB_WALKING') NOT NULL DEFAULT 'DEFAULT',
  `message` varchar(10000) NOT NULL DEFAULT '0',
  `item` varchar(100) DEFAULT NULL,
  `server` varchar(50) DEFAULT NULL,
  `player` varchar(50) DEFAULT NULL,
  `fixed` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=808110 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel logging.log_copy wordt geschreven
CREATE TABLE IF NOT EXISTS `log_copy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` enum('DEFAULT','GOLD_TRANSFER','ERROR','DEPOSIT_BANK_ITEM','CAPTCHA','WEB_WALKING') NOT NULL DEFAULT 'DEFAULT',
  `message` varchar(10000) NOT NULL DEFAULT '0',
  `item` varchar(100) DEFAULT NULL,
  `server` varchar(50) DEFAULT NULL,
  `player` varchar(50) DEFAULT NULL,
  `fixed` tinyint(4) DEFAULT NULL,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2370298 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;

-- Data exporteren was gedeselecteerd

-- Databasestructuur van proxies wordt geschreven
CREATE DATABASE IF NOT EXISTS `proxies` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `proxies`;

-- Structuur van  tabel proxies.proxies wordt geschreven
CREATE TABLE IF NOT EXISTS `proxies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_addres` varchar(50) NOT NULL,
  `port` varchar(50) NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `mule_proxy` tinyint(4) NOT NULL DEFAULT 0,
  `is_alive` tinyint(4) NOT NULL DEFAULT 0,
  `error_ip` tinyint(4) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1053 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd

-- Databasestructuur van server_muling wordt geschreven
CREATE DATABASE IF NOT EXISTS `server_muling` /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `server_muling`;

-- Structuur van  tabel server_muling.account wordt geschreven
CREATE TABLE IF NOT EXISTS `account` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `password` varchar(50) DEFAULT NULL,
  `bank_pin` varchar(50) DEFAULT NULL,
  `visible` varchar(50) DEFAULT 'true',
  `trade_with_other` varchar(50) DEFAULT NULL,
  `country_code_proxy_usage` varchar(50) DEFAULT NULL,
  `account_stage` enum('TUT_ISLAND','QUEST_COOK_ASSISTANT','QUEST_ROMEO_AND_JULIET','UNKNOWN','QUEST_SHEEP_SHEARER','MINING_LEVEL_TO_15','MINING_IRON_ORE','MULE_TRADING','RIMMINGTON_IRON_ORE','GE_SELL_BUY_MINING','QUEST_DORICS_QUEST','WOODCUTTING_GOLD_FARM') DEFAULT 'TUT_ISLAND',
  `account_stage_progress` int(11) DEFAULT 0,
  `login_status` enum('INITIALIZING','LOGGED_IN','DEFAULT') DEFAULT 'DEFAULT',
  `status` enum('BANNED','AVAILABLE','LOCKED','MANUAL_REVIEW','NOT_VERIFIED','TIMEOUT','LOCKED_INGAME','WALKING_STUCK','TASK_TIMEOUT','OUT_OF_MONEY','MULE','LOCKED_TIMEOUT','INVALID_PASSWORD','SUPER_MULE','SERVER_MULE') DEFAULT 'AVAILABLE',
  `account_value` int(11) DEFAULT 0,
  `quest_points` int(11) DEFAULT 0,
  `day` int(11) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `email` varchar(200) DEFAULT NULL,
  `proxy_ip` varchar(50) DEFAULT NULL,
  `proxy_port` varchar(50) DEFAULT NULL,
  `world_number` int(11) DEFAULT NULL,
  `low_cpu_mode` tinytext DEFAULT NULL,
  `amount_timeout` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `break_till` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9012 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel server_muling.account_banned wordt geschreven
CREATE TABLE IF NOT EXISTS `account_banned` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `password` varchar(50) DEFAULT NULL,
  `bank_pin` varchar(50) DEFAULT NULL,
  `visible` varchar(50) DEFAULT 'true',
  `trade_with_other` varchar(50) DEFAULT NULL,
  `country_code_proxy_usage` varchar(50) DEFAULT NULL,
  `account_stage` enum('TUT_ISLAND','QUEST_COOK_ASSISTANT','QUEST_ROMEO_AND_JULIET','UNKNOWN','QUEST_SHEEP_SHEARER','MINING_LEVEL_TO_15','MINING_IRON_ORE','MULE_TRADING','RIMMINGTON_IRON_ORE','GE_SELL_BUY_MINING','QUEST_DORICS_QUEST','WOODCUTTING_GOLD_FARM') DEFAULT 'TUT_ISLAND',
  `account_stage_progress` int(11) DEFAULT 0,
  `login_status` enum('INITIALIZING','LOGGED_IN','DEFAULT') DEFAULT 'DEFAULT',
  `status` enum('BANNED','AVAILABLE','LOCKED','MANUAL_REVIEW','NOT_VERIFIED','TIMEOUT','LOCKED_INGAME','WALKING_STUCK','TASK_TIMEOUT','OUT_OF_MONEY','MULE','LOCKED_TIMEOUT','INVALID_PASSWORD','SUPER_MULE','SERVER_MULE') DEFAULT 'AVAILABLE',
  `account_value` int(11) DEFAULT 0,
  `quest_points` int(11) DEFAULT 0,
  `day` int(11) DEFAULT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `email` varchar(200) DEFAULT NULL,
  `proxy_ip` varchar(50) DEFAULT NULL,
  `proxy_port` varchar(50) DEFAULT NULL,
  `world_number` int(11) DEFAULT NULL,
  `low_cpu_mode` tinytext DEFAULT NULL,
  `amount_timeout` int(11) DEFAULT 0,
  `created_at` datetime DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `break_till` datetime DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8969 DEFAULT CHARSET=latin1 ROW_FORMAT=COMPACT;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel server_muling.config wordt geschreven
CREATE TABLE IF NOT EXISTS `config` (
  `used_by_database` varchar(50) DEFAULT NULL,
  `id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
-- Structuur van  tabel server_muling.proxies wordt geschreven
CREATE TABLE IF NOT EXISTS `proxies` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip_addres` varchar(50) NOT NULL,
  `port` varchar(50) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `mule_proxy` tinyint(4) NOT NULL DEFAULT 0,
  `is_alive` tinyint(4) NOT NULL DEFAULT 0,
  `error_ip` tinyint(4) NOT NULL DEFAULT 0,
  `updated_at` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=977 DEFAULT CHARSET=latin1;

-- Data exporteren was gedeselecteerd
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
```
