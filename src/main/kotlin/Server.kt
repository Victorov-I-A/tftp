import java.io.*
import java.lang.Exception
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.util.*


fun main(args: Array<String>) {
    val s = Server();
    s.start()
}

class Server {
    fun start() {
        //включились, ждем от клиента
        val server = ServerSocket(9999)
        val client = server.accept()
        val dataInputStream = DataInputStream(client.getInputStream())

        val length: Int = dataInputStream.readInt() // read length of incoming message
        val input = ByteArray(length)
        client.getInputStream().read(input)
        val request = Utils.unpackRequest(input) //в утиле всё написал мб

        if (request.first == Utils.Opcode.RRQ) readMode(request, client)
        if (request.first == Utils.Opcode.WRQ) writeMode(request, client)

    }

    private fun writeMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        val dataInputStream = DataInputStream(client.getInputStream())
        val dataOutputStream = DataOutputStream(client.getOutputStream())

        val byteStream = ByteArrayOutputStream()
        var currentBlock: Short = 0

        val toWrite = Utils.packACK(currentBlock)
        dataOutputStream.writeInt(toWrite.size)
        dataOutputStream.write(toWrite)

        while (true) {
            val arraySize = dataInputStream.readInt();
            val input = ByteArray(arraySize)
            dataInputStream.read(input)

            val inputOnlyData = ByteArray(arraySize - 4)
            System.arraycopy(input, 4, inputOnlyData, 0, arraySize - 4);
            byteStream.write(inputOnlyData)

            val packData = Utils.unpackData(input)
            currentBlock++
            if (packData.second.size < 512) {

                val fileName = request.second[0].split('/').last()
                File(fileName).writeBytes(byteStream.toByteArray())
                println("File $fileName downloaded successfully.")
                return
            }

            println("pacack")
            val toWrite = Utils.packACK(currentBlock)
            dataOutputStream.writeInt(toWrite.size)
            dataOutputStream.write(toWrite)

        }
    }

    private fun readMode(request: Pair<Utils.Opcode, List<String>>, client: Socket) {
        val dataInputStream = DataInputStream(client.getInputStream())
        val dataOutputStream = DataOutputStream(client.getOutputStream())

        val file: File
        var fileBytes = ByteArray(0)
        try {
            file = File(request.second[0])
            fileBytes = Files.readAllBytes(file.toPath()) //кучи говн
        } catch (e: Exception) {
            val z = Utils.packError()
            dataOutputStream.writeInt(z.size)
            dataOutputStream.write(z)
            println("no such file")
        }
        println(request)
        var remainingBytes = fileBytes.size//оставшиеся байты
        var currentBlock: Short = 1//текущий блок
        while (true) {
            var minn = 512 * currentBlock
            if (remainingBytes < 512) minn = remainingBytes + 512 * (currentBlock - 1)
            val blockData = Arrays.copyOfRange(
                fileBytes,
                512 * (currentBlock - 1), minn
            )

            val z = Utils.packDataBlock(currentBlock, blockData)
            dataOutputStream.writeInt(z.size)
            dataOutputStream.write(z)

            val arraySize = dataInputStream.readInt();
            val readValue = ByteArray(arraySize)
            dataInputStream.read(readValue)

            remainingBytes -= 512
            currentBlock++
        }
    }
}