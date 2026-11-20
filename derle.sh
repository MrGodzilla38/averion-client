#!/usr/bin/env bash
set -euo pipefail

cd -- "$(dirname -- "${BASH_SOURCE[0]}")"

path_jdk=
path_javac=$(command -v javac || true)
if [[ -n "$path_javac" ]]; then
    path_jdk=$(dirname -- "$(dirname -- "$(readlink -f -- "$path_javac")")")
fi

jdk_candidates=(
    "${JAVA_HOME:-}"
    "$path_jdk"
    /usr/lib/jvm/*
    "${SDKMAN_DIR:-$HOME/.sdkman}"/candidates/java/*
    "$HOME"/.jdks/*
    "${XDG_DATA_HOME:-$HOME/.local/share}"/Polyfrost/OneClient/metadata/java/*/*
)
jdk_home=
for candidate in "${jdk_candidates[@]}"; do
    if [[ -n "$candidate" && -x "$candidate/bin/java" && -x "$candidate/bin/javac" && -r "$candidate/release" ]] &&
        grep -qE '^JAVA_VERSION="25([.+-][^"]*)?"$' "$candidate/release"; then
        jdk_home=$candidate
        break
    fi
done

if [[ -z "$jdk_home" ]]; then
    printf 'JDK 25 bulunamadı. JDK 25 kurup JAVA_HOME değişkenini kurulum dizinine ayarlayın.\n' >&2
    exit 1
fi

export JAVA_HOME="$jdk_home"
printf 'JDK 25 kullanılıyor (%s).\n' "$JAVA_HOME"

versions=$(python3 -c 'import json, sys; print(*json.load(sys.stdin), sep="\n")' < gradle/minecraft-targets.json)
output_dir=sürümler
mkdir -p -- "$output_dir"

while IFS= read -r version; do
    printf '\nMinecraft %s derleniyor…\n' "$version"
    if ! ./gradlew "$@" "-PaverionTarget=$version" ":targets:$version:build" </dev/null; then
        printf 'Minecraft %s derlenemedi.\n' "$version" >&2
        exit 1
    fi

    jar_name="averion-client-$version.jar"
    cp -- "targets/$version/build/libs/$jar_name" "$output_dir/$jar_name"
done <<< "$versions"

printf '\nTüm JAR dosyaları %s/ klasörüne kopyalandı.\n' "$output_dir"
