from flask import Flask, jsonify, request 

app = Flask(__name__)
consultas = [
    {"id": 1, "medico_id": 1, "paciente": "Maria Souza", "data": "2026-11-18", "hora": "14:30", "motivo": "Retorno"},
    {"id": 2, "medico_id": 1, "paciente": "João Lima", "data": "2026-11-19", "hora": "09:00", "motivo": "Check-up"},
    {"id": 3, "medico_id": 2, "paciente": "Ana Prado", "data": "2026-11-18", "hora": "10:15", "motivo": "Dor de Cabeça"},

]

@app.route("/")
def inicio():
    return jsonify({"mensagem": "Olá, a API do MedAgenda está funcionando!"})
@app.route("/consultas")
def listar_consultas():
    return jsonify(consultas)
@app.route("/medicos/<int:medico_id>/consultas")  #
def consultas_do_medico(medico_id):
    data = request.args.get("data")
    resultado = []                 #lista vazia 
    for consulta in consultas:     # olha uma ficha de cada vez 
        if consulta["medico_id"] == medico_id:  # essa ficha é do medico do endereco?
            if data is None or consulta["data"] == data:
                resultado.append(consulta)           # se for guarda na lista #append é adicionar no final da lista  
    return jsonify(resultado) # devolve so as fichas guardadas 
if __name__ == "__main__":
    app.run(debug=True)
