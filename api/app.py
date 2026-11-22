from flask import Flask, jsonify, request
import sqlite3

app = Flask(__name__)


#  BANCO DE DADOS (o "fichário") 
def conectar():
    # abre a porta do fichário
    conexao = sqlite3.connect("medagenda.db")
    return conexao


def criar_tabela():
    # cria o fichário com as colunas, se ainda não existir
    conexao = conectar()
    conexao.execute("""
        CREATE TABLE IF NOT EXISTS consultas (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            medico_id INTEGER NOT NULL,
            paciente TEXT NOT NULL,
            data TEXT NOT NULL,
            hora TEXT NOT NULL,
            motivo TEXT
        )
    """)
    conexao.commit()
    conexao.close()


criar_tabela()

#  ROTA DE TESTE 
@app.route("/")
def inicio():
    return jsonify({"mensagem": "Olá, a API do MedAgenda está funcionando!"})


# LISTAR TODAS AS CONSULTAS (GET) 
@app.route("/consultas")
def listar_consultas():
    conexao = conectar()
    conexao.row_factory = sqlite3.Row 
    linhas = conexao.execute("SELECT * FROM consultas ORDER BY data, hora").fetchall()
    conexao.close()
    return jsonify([dict(linha) for linha in linhas])


#  AGENDA DO MÉDICO (GET com filtros)
@app.route("/medicos/<int:medico_id>/consultas")
def consultas_do_medico(medico_id):
    data = request.args.get("data")
    inicio = request.args.get("inicio")
    fim = request.args.get("fim")

    #abre o fichario e pega as fichas deste medico

    conexao = conectar()
    conexao.row_factory = sqlite3.Row 
    linhas = conexao.execute(
        "SELECT * FROM consultas WHERE medico_id = ? ORDER BY data, hora", (medico_id,)
    ).fetchall()
    conexao.close()

    #transforma cada liinha do bando em uma ficha (dicionario), como as de antes 
    fichas = []
    for linha in linhas:
        fichas.append(dict(linha))

    #os filtros de data continuam iguais
    resultado = []
    for consulta in fichas:
        if data is None or consulta["data"] == data:
            if inicio is None or consulta["data"] >= inicio:
                if fim is None or consulta["data"] <= fim:
                    resultado.append(consulta)
    return jsonify(resultado)



#  CRIAR CONSULTA (POST)
# Agora grava no banco (o fichário), não mais na lista.
@app.route("/consultas", methods=["POST"])
def criar_consulta():
    nova = request.get_json()                        # ficha preenchida que veio na requisição

    conexao = conectar()                             # abre o fichário
    cursor = conexao.execute(
        "INSERT INTO consultas (medico_id, paciente, data, hora, motivo) VALUES (?, ?, ?, ?, ?)",
        (nova["medico_id"], nova["paciente"], nova["data"], nova["hora"], nova.get("motivo"))
    )
    conexao.commit()                                 # "pode arquivar"
    nova["id"] = cursor.lastrowid                    # número que o banco deu à ficha
    conexao.close()                                  # fecha a gaveta

    return jsonify(nova), 201                        # 201 = "criado com sucesso"


# --- LIGAR O SERVIDOR ---
if __name__ == "__main__":
    app.run(debug=True)