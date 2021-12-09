import java.io.*
import java.lang.Exception
import java.net.Socket
import java.net.SocketException
import java.nio.file.Files

fun main(args: Array<String>) {
    val c = Client();
    c.start()
}

//C:/Users/Ilia/k.png
class Client {
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
                    "exit" -> {
                        return
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
            if (splittedInput?.size!! > 2) {
                println("format: [r | w] pathname")
                continue
            }

        }
    }

    private fun readFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val dataOutputStream = client.getOutputStream()
        val dataInputStream = client.getInputStream()

        val request = Utils.packRequest(Utils.Opcode.RRQ, filePath)
        dataOutputStream.write(request)

        val fileContent = ByteArrayOutputStream()
        var currentBlock: Short = 1
        var readBytes: Int
        while (true) {
            val input = ByteArray(Utils.PACKET_SIZE)
            try {
                readBytes = dataInputStream.read(input)
            } catch (e: SocketException) {
                TODO("Not yet implemented")
            }
            when (input[1].toShort()) {
                Utils.Opcode.Error.code -> {
                    TODO("Not yet implemented")
                }
                Utils.Opcode.Data.code -> {
                    val packData = Utils.unpackData(input)
                    if (currentBlock == packData.first) {
                        fileContent.write(input.copyOfRange(4, readBytes))
                        if (readBytes != Utils.PACKET_SIZE) {
                            break
                        }
                        currentBlock++
                    }
                    dataOutputStream.write(Utils.packACK(currentBlock))
                }
                else -> {
                    TODO("Not yet implemented")
                }
            }
        }
        client.close()
        val fileName = filePath.split('/').last()
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun writeFile(filePath: String) {
        val client = Socket("127.0.0.1", 9999)
        val dataOutputStream = DataOutputStream(client.getOutputStream())
        val dataInputStream = DataInputStream(client.getInputStream())

        val request = Utils.packRequest(Utils.Opcode.WRQ, filePath)
        dataOutputStream.writeInt(request.size)
        dataOutputStream.write(request)

        val file: File
        val fileBytes: ByteArray
        try {
            file = File(filePath)
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            println("no such file")
            return
        }
        var remainingBytes = fileBytes.size//оставшиеся байты
        var currentBlock: Short = 1//текущий блок

        while (true) {
            val arraySize = dataInputStream.readInt();
            val readValue = ByteArray(arraySize)
            dataInputStream.read(readValue)

            var minn = 512 * currentBlock
            if (remainingBytes < 512) minn = remainingBytes + 512 * (currentBlock - 1)
            println(minn)
            println("_____")
            val blockData = fileBytes.copyOfRange(512 * (currentBlock - 1), minn)

            val z = Utils.packDataBlock(currentBlock, blockData)
            dataOutputStream.writeInt(z.size)
            dataOutputStream.write(z)


            remainingBytes -= 512
            currentBlock++
            if (remainingBytes <= 0) break
        }
    }

    //ыаы ты думал тут что-то будет?)))000))да. Чел ты смишной. Аруу:))))
    private fun closeClient() {
        println(123)
        TODO("Not yet implemented")
    }

    private fun closeServer() {
        TODO("Not yet implemented")
    }
}