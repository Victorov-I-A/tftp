import java.math.BigInteger
import java.nio.ByteBuffer

object Utils {
    const val NETASCII_MODE = "netascii"
    const val OCTET_MODE = "octet"

    val CHARSET = Charsets.UTF_8

    enum class Opcode(val code: Short) {
        RRQ(1),
        WRQ(2),
        Data(3),
        ACK(4),
        Error(5)
    }

    enum class ErrorCode(val code: Short) {
        NotDefined(0),
        FileNotFound(1),
        AccessViolation(2),
        DiskFullOrAllocationExceeded(3),
        IllegalTFTPOperation(4),
        UnknownTransferID(5),
        FileAlreadyExists(6),
        NoSuchUser(7)
    }

    fun packRequest(opcode: Opcode, fileName: String, mode: String = OCTET_MODE): ByteArray {
        return ByteBuffer.allocate(2).putShort(opcode.code).array() +
                fileName.toByteArray(CHARSET) + '0'.code.toByte() + mode.toByteArray(CHARSET) + '0'.code.toByte()
    }

    //все проблемы могут быть только с байтбуффером, его нормально не декодировать, н оя вроже везде его юзаю только для изичных флагов и файласодержимого
    fun packDataBlock(num: Short, content: ByteArray): ByteArray = ByteBuffer.allocate(4 + content.size)
        .putShort(Opcode.Data.code)
        .putShort(num)
        .put(content)
        .array()

    fun packACK(num: Short): ByteArray = ByteBuffer.allocate(4)
        .putShort(Opcode.ACK.code)
        .putShort(num)
        .array()

    //вот тут либо то, либо сё, вообще на третье должна быт ьпроверка, но это ладно
    //возвращает пару, где первое понятно что, а второе это список из названия файла и мода
    fun unpackRequest(bytes: ByteArray): Pair<Opcode, List<String>> =
        if (BigInteger(bytes.copyOfRange(0, 2)).toShort() == Opcode.RRQ.code)
            Pair(Opcode.RRQ, bytes.copyOfRange(2, bytes.size).toString(CHARSET).split('0').dropLast(1))
        else
            Pair(Opcode.WRQ, bytes.copyOfRange(2, bytes.size).toString(CHARSET).split('0').dropLast(1))

    //возвращает номер блока и его осдержимое в паре
    fun unpackData(bytes: ByteArray): Pair<Short, ByteArray> =
        Pair(BigInteger(bytes.copyOfRange(2, 4)).toShort(), bytes.copyOfRange(4, bytes.size))

    fun unpackACK(bytes: ByteArray): Short = BigInteger(bytes.copyOfRange(2, 4)).toShort()
}