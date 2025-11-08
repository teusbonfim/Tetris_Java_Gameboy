# 1. Limpa builds antigos
Write-Host "Limpando builds antigos (pasta bin/ e arquivos .class)..."
if (Test-Path -Path "bin") {
    # tenta remover recusivamente, mas ignora erros (arquivos em uso/permissões)
    Remove-Item -Path "bin" -Recurse -Force -ErrorAction SilentlyContinue
}
# remove arquivos .class antigos, ignorando erros se houver arquivos em uso
Get-ChildItem -Path "src" -Filter "*.class" -Recurse | ForEach-Object {
    Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
}

# 2. Cria a pasta de 'build' (saída)
Write-Host "Criando pasta bin..."
New-Item -ItemType Directory -Path "bin" -Force | Out-Null

# 3. Compila TODOS os arquivos .java da 'src' para dentro da 'bin'
Write-Host "Compilando código-fonte (.java)..."
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse
# cria um arquivo temporário com a lista de fontes (isso evita problemas com caminhos com espaços)
$sourcesList = "$PWD\sources.txt"
if (Test-Path $sourcesList) { Remove-Item $sourcesList -Force -ErrorAction SilentlyContinue }
# coloca cada caminho entre aspas para que javac aceite nomes com espaços
$javaFiles | ForEach-Object { '"' + $_.FullName + '"' } | Out-File -FilePath $sourcesList -Encoding ascii
# usa o recurso @file do javac para ler a lista de fontes
$compileCommand = "javac --release 21 -d bin -cp src @$sourcesList"
Write-Host "Executando: $compileCommand"
Invoke-Expression $compileCommand
# limpa o arquivo temporário
if (Test-Path $sourcesList) { Remove-Item $sourcesList -Force -ErrorAction SilentlyContinue }

# 4. Verifica se a compilação falhou
if ($LASTEXITCODE -ne 0) {
    Write-Host "----------------------------------------"
    Write-Host "ERRO: Falha na compilação."
    Write-Host "Corrija os erros de código e tente novamente."
    Write-Host "----------------------------------------"
    exit 1
}

# 5. Copia TODOS os recursos para a pasta 'bin'
Write-Host "Copiando todos os recursos (áudio, txt, etc.) para a pasta 'bin'..."
Get-ChildItem -Path "src" -Exclude "*.java" -Recurse | ForEach-Object {
    $targetPath = $_.FullName.Replace("src", "bin")
    if ($_.PSIsContainer) {
        New-Item -ItemType Directory -Path $targetPath -Force | Out-Null
    } else {
        Copy-Item $_.FullName -Destination $targetPath -Force
    }
}
# Copia também o highscore.txt da raiz
Copy-Item "highscore.txt" -Destination "bin" -Force

# 6. Executa o programa!
Write-Host "----------------------------------------"
Write-Host "Build concluído. Iniciando o Jogo..."
Write-Host "----------------------------------------"
java --enable-preview -cp "bin;lib/sqlite-jdbc-3.51.0.0.jar" com.tetris.Main