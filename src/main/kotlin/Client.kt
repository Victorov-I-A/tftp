import java.io.*
import java.net.Socket
import java.util.*

fun main(args: Array<String>) {
    val c = Client();
    c.start()
}

class Client {
    // тут всё по рофлу, и единственное что пока +- работает - при вводе r PATHNAME - вызывается readFile
    fun start() {
        println("format: [r | w] pathname")
        while (true) {
            val input = readLine()?.trim()?.replace("\\s+".toRegex(), " ")

            val splittedInput = input?.split(" ")
            if (splittedInput?.get(0) == "") {
                continue
            }


            if (splittedInput?.size == 1) {
                when ((splittedInput[0])) {
                    "close" -> {
                        closeClient()
                    }
                    "closeServer" -> {
                        closeServer()
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }
            }
            if (splittedInput?.size == 2) {
                val inp0 = splittedInput[0]
                val inp1 = splittedInput[1]
                when (inp0) {
                    "r" -> {
                        readFile(inp1)
                    }
                    "w" -> {
                        writeFile(inp1)
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }

            }

        }
    }

    private fun readFile(filePath: String) {
        //подключились, отправили поток байтов в котором содержится
        // сообщение "RRQ C://l1/c.jpg modeName" и ждем ответа от сервера
        val client = Socket("127.0.0.1", 9999)
        val charset = Charsets.UTF_8
        val dOut = DataOutputStream(client.getOutputStream())
        val msg = "RRQ $filePath modeName"
        val message = msg.toByteArray(charset)
        dOut.writeInt(message.size)
        dOut.write(message);

        //получаем поток байтов, и превращаем в файл
        // а надо сделать чтобы мы поулчили кусочек байтов, и отправили запрос на некст кусочек,
        // пока не получим все кусочки и тогда собрать их и получить файл
        val dIn = DataInputStream(client.getInputStream())
        val length: Int = dIn.readInt()
        val messageInBytes = ByteArray(length)
        dIn.read(messageInBytes)
        try {
            FileOutputStream(
                "fff" +
                        Random().nextInt(1000000)
            ).use { fos -> fos.write(messageInBytes) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        val charset = Charsets.UTF_8
        val dOut = DataOutputStream(client.getOutputStream())

        val msg = "WRQ $filePath modeName"
        val message = msg.toByteArray(charset)
        dOut.writeInt(message.size); // write length of the message
        dOut.write(message);           // write the message

    }

    private fun closeClient() {
        println(123)
        TODO("Not yet implemented")
    }

    private fun closeServer() {
        TODO("Not yet implemented")
    }
}