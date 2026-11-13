#!/usr/bin/env bash
set -euo pipefail

modo="${1:-jogar}"
case "$modo" in
  compilar|testar|jogar) ;;
  *) echo 'Uso: bash executar.sh [compilar|testar|jogar]' >&2; exit 2 ;;
esac

base="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd -- "$base"
mkdir -p out/classes
mapfile -d '' -t fontes < <(find src tests -type f -name '*.java' -print0)
if command -v javac >/dev/null 2>&1; then
  compilador=(javac)
else
  compilador=(java -m jdk.compiler/com.sun.tools.javac.Main)
fi
"${compilador[@]}" -encoding UTF-8 -Xlint:all -Werror -d out/classes "${fontes[@]}"
case "$modo" in
  compilar) echo 'Compilação concluída sem avisos.' ;;
  testar) java -ea -cp out/classes solidexercicio10.tests.TestesFase01 ;;
  jogar) java -cp out/classes solidexercicio10.Main ;;
esac
