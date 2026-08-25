package com.hcc.tfm_hcc.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Servicio para el cifrado y descifrado del contenido binario de los archivos
 * clínicos almacenados en MongoDB.
 *
 * <p>Se mantiene independiente de {@code AESEncryptionConverter} porque ese
 * conversor está pensado para columnas de texto de base de datos (cifrado
 * determinista), mientras que el contenido de un archivo no necesita
 * consultarse por igualdad y se beneficia de un cifrado autenticado no
 * determinista (AES/GCM con un vector de inicialización distinto por archivo).</p>
 */
public interface ArchivoCifradoService {

    /**
     * Cifra el contenido leído de {@code entrada} y lo escribe en {@code salida}.
     *
     * @param entrada contenido en claro del archivo subido por el usuario
     * @param salida  destino donde se escribe el contenido cifrado
     * @throws IOException si falla la lectura, la escritura o el propio cifrado
     */
    void cifrar(InputStream entrada, OutputStream salida) throws IOException;

    /**
     * Envuelve el contenido cifrado de {@code entradaCifrada} en un flujo que
     * produce su contenido ya descifrado, listo para servir al usuario propietario.
     *
     * @param entradaCifrada flujo con el contenido cifrado tal y como se guardó en MongoDB
     * @return flujo de lectura con el contenido en claro
     * @throws IOException si falla la lectura del flujo o el propio descifrado
     */
    InputStream descifrar(InputStream entradaCifrada) throws IOException;
}
