"""Launch the packaged app without JAVA_HOME or Java on PATH; fail on early exit."""
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import time

root = Path(sys.argv[1]).resolve()
if sys.platform == 'darwin':
    executable = root / 'Tetris.app/Contents/MacOS/Tetris'
    runtime = root / 'Tetris.app/Contents/runtime'
elif sys.platform == 'win32':
    executable = root / 'Tetris/Tetris.exe'
    runtime = root / 'Tetris/runtime'
else:
    executable = root / 'Tetris/bin/Tetris'
    runtime = root / 'Tetris/lib/runtime'
assert runtime.is_dir(), f'Missing bundled runtime: {runtime}'
env = os.environ.copy()
env.pop('JAVA_HOME', None)
env['PATH'] = os.path.join(env.get('SystemRoot', 'C:\\Windows'), 'System32') if os.name == 'nt' else '/usr/bin:/bin'
with tempfile.TemporaryDirectory(prefix='tetris-launch-') as cwd:
    with tempfile.TemporaryFile() as log:
        process = subprocess.Popen([str(executable)], cwd=cwd, env=env, stdout=log, stderr=log)
        try:
            time.sleep(8)
            if process.poll() is not None:
                log.seek(0)
                raise RuntimeError(f'App exited with {process.returncode}: {log.read().decode(errors="replace")}')
            print(f'PASS: packaged process remains running with bundled runtime: {executable}')
        finally:
            if process.poll() is None:
                process.terminate()
                try:
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
