import pytest
import sys

def main():
    return pytest.main(["-x", *sys.argv[1:]])
    raise SystemExit("use pytest instead")

if __name__ == "__main__":
    raise SystemExit(main())