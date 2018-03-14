#配置DEMO:

    DEFAULT_CONFIG reset loggerConfig {
        loggers {
            default {
                formatter use "default"
                targets += "Console"
            }
    
            "test" <= {
                baseOn("default")
                targets += file("test.log")
                targets += console()
            }
    
            logger("test2") {
                formatter use "simple"
                targets += file("test2.log")
                targets += "test3"
                level { WARN }
            }
    
            "test3" += arrayOf("test", "test2")
        }
        formatters {
            default = format { "[$datetime-$level]$log\n" }
 
            "simple" to format { "$log\n" }
            "simple2" <= format { "$log\n" }
        }
        targets {
            default = console()
            "test3" <= file("test3.log")
        }
    }
    
#使用Logger
    Logger.getLogger().info("Log Something Here")