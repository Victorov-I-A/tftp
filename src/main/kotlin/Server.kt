import java.io.*
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.inputStream


fun main(args: Array<String>) {
    val s = Server();
    s.start()
}

class Server {
    fun start() {
        //включились, ждем от клиента
        val server = ServerSocket(9999)
        val client = server.accept()
        val dIn = DataInputStream(client.getInputStream())
        val length: Int = dIn.readInt() // read length of incoming message

        //клиент отправил "RRQ C://l1/c.jpg modeName" - берем путь файла, нахожим файл у нас, берем его, переводим в байты
        if (length > 0) {
            val charset = Charsets.UTF_8
            val messageInBytes = ByteArray(length)
            dIn.read(messageInBytes)
            val messageStr = messageInBytes.toString(charset)
            println(messageStr)
            val file = File(messageStr.split(" ")[1])
            val fileBytes = Files.readAllBytes(file.toPath())

            //и отправляем поток байтов (сам файл)
            val dOut = DataOutputStream(client.getOutputStream())
            dOut.writeInt(fileBytes.size); // write length of the message
            dOut.write(fileBytes);

            //а нам надо отправить часть байта и ждать некст сообщение, тут вот я научился разбить поток байтов чтобы взять кусочек
            // и надо будет отправлять рекурсивно кусочки
            println(fileBytes.size)
            val x = Arrays.copyOfRange(fileBytes, 0, 512)
            println(fileBytes.size)
            println(x.size)

        }

    }
}