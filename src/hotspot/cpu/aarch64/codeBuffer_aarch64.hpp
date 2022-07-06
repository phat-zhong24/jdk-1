/*
 * Copyright (c) 2002, 2022, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2014, Red Hat Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef CPU_AARCH64_CODEBUFFER_AARCH64_HPP
#define CPU_AARCH64_CODEBUFFER_AARCH64_HPP

public:
  class SharedStubToRuntimeCallRequest {
   private:
    address _dest;
    int _caller_offset;

   public:
    SharedStubToRuntimeCallRequest(address dest = nullptr, int caller_offset = -1):
        _dest(dest),
        _caller_offset(caller_offset) {}

    address dest()      const { return _dest; }
    int caller_offset() const { return _caller_offset; }
  };

private:
  void pd_initialize() {}
  bool pd_finalize_stubs();

public:
  void flush_bundle(bool start_new_bundle) {}
  static constexpr bool supports_shared_stubs() { return true; }

  void shared_stub_to_runtime_for(address dest, int caller_offset);

#endif // CPU_AARCH64_CODEBUFFER_AARCH64_HPP
